package solver;

import java.util.List;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import grid.Assignment;
import grid.AssignmentMethod;
import grid.Box;
import grid.Cell;
import grid.CellSet;
import grid.Column;
import grid.Row;
import grid.Symbol;

abstract class Method {	
	Solver m_solver;
	
	Method(Solver solver) {
		m_solver = solver;
	}

	/**
	 * Every class extending the Method class provides an applyMethod method to try to make some progress on solving the Sudoku.
	 *  
	 * @param stepNumber What is the current step number ?
	 * @param actions List to add diagnostic strings to, recording actions performed 
	 * @return True if progress has been made.
	 * @throws IllegalAssignmentException A cell-to-symbol assignment failed.
	 */
	abstract boolean applyMethod(int stepNumber, List<String> actions) throws IllegalAssignmentException;
}

// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------

/**
 * Class which tries to find symbols which are now limited to being assignable to only one specific cell in a cellset (column, row or box).
 */
class Method1 extends Method {
	
	Method1(Solver solver) {
		super(solver);
	}
	
	// Look for and assign the first case found of a symbol which can only be applied to one specific cell in a cellset.
	//
	// For example:
	//                                    	
	// x	x	x		x	x	x		3	=1	.	
	// x	x	x		x	x	1		.	.	.	
	// 1	x	x		x	x	x		.	.	5	
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	1
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		1	x	x
	// x	x	x		x	x	x		x	x	x
	//
	// For the top-right box (Box 3), the symbol 1 can only be in the middle cell of the top row, shown as '=1' above.
	
	boolean applyMethod(int stepNumber, List<String> actions) throws IllegalAssignmentException {		
		boolean changedState = false;		
		// Try each cellset in turn until we've performed an assignment.
		for(CellSetAssessment csa : m_solver.cellSetAssessments()) {			
			// Functional approach to finding an unassigned symbol with only one possible cell left		
			Symbol symbol = csa.symbols().stream()
								.filter(s -> !csa.symbolAlreadyAssigned(s))
								.filter(s -> csa.couldBeCellCount(s) == 1)
								.findFirst().orElse(null);	// Returns null if no such symbol found.

			// If we've found a symbol that can be assigned. Extract the cell it can be assigned to and make the assignment.
			if(symbol != null) {
				Cell onlyAvailableCell = csa.couldBeCellsForSymbol(symbol).get(0);
				String detail = "Only cell available for this symbol in " + csa.getRepresentation();
				Assignment assignment = new Assignment(onlyAvailableCell, symbol, AssignmentMethod.AutomatedDeduction, detail,  stepNumber);
				m_solver.performAssignment(assignment);
				changedState = true;
				String s = "Assigned symbol " + symbol.getRepresentation() + 
						   " to cell " + onlyAvailableCell.getGridLocationString() + 
						   " for " + csa.getRepresentation() + 
						   " - no other cell is available for this symbol";
				actions.add(s);
			}
			if(changedState) break;		// Only make one assignment for each attempt to apply the method.
		}
		return changedState;
	}	
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

/**
 * Class which tries to find cells which are now limited to being assignable to only one specific symbol.
 */
class Method2 extends Method {
	
	Method2(Solver solver) {
		super(solver);
	}
	
	// Look for and apply the first case found where only one symbol is now left as a possible assignment for the cell. 
	//
	// For example:
	//                                    	
	// 4	.	.		x	x	x		x	x	x	
	// =2	6	3		5	x	x		x	x	x	
	// 1	7	.		x	x	x		x	x	x	
	//
	// x	x	x		x	x	x		x	x	x
	// 9	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// 8	x	x		x	x	x		x	x	x
	//
	// For the top-left box (Box 1), the middle cell of the first column can only be a '2', shown as =2, all the other
	// symbols have been ruled out.
	
	boolean applyMethod(int stepNumber, List<String> actions) throws IllegalAssignmentException {		
		boolean changedState = false;
		for(CellAssessment ca : m_solver.cellAssessments()) {
			if(!ca.isAssigned() && (ca.couldBeCount() == 1)) {
				Symbol onlyAvailableSymbol = ca.couldBeSymbols().get(0);
				Assignment assignment = new Assignment(ca.cell(), onlyAvailableSymbol, 
								AssignmentMethod.AutomatedDeduction, "Only symbol still available for this cell", stepNumber);
				m_solver.performAssignment(assignment);
				changedState = true;
				String s = "assigned symbol " + onlyAvailableSymbol.getRepresentation() +  
						   " to cell " + ca.cell().getGridLocationString() +
						   " - no other symbol can be assigned to this cell";
				actions.add(s);
			}
			if(changedState) break;		// Only make one assignment for each attempt to apply the method.			
		}			
		return changedState;
	}
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

/**
 * Class which tries to rule out cell/symbol combinations based on how the available cells for symbols in one cellset (box, row, column) 
 * restrict the possibilities in the cellsets that intersect with them.  
 */
class Method3 extends Method {
	
	Method3(Solver solver) {
		super(solver);
	}
	
	// Look through each box to see where a particular unresolved symbol can only appear in a specific row or column of the box.
	// Where this arises, we can rule-out the symbol from the other cells in the row or column which are not in the box.
	//
	// For example (x or * can be a known or unknown value)
	//                                  C7  C8  C9	
	// x	x	x		x	x	x		3	1	.	R1
	// *	*	*		*	*	*		.	.	.	R2
	// x	7	x		x	x	x		.	.	5	R3
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	7
	//
	// For the top-right box (Box 3), we can see that the 7 must only be in Row 2. 
	// Consequently we can rule out 7 from being any of the cells in Row 2 which do not intersect with Box 3 - the 
	// cells marked with a * cannot be a 7, and we can apply this 7-is-ruled-out restriction to those cells.
	//
	// The example above shows the situation in a box restricting cells in a row which intersects with it. Similar
	// logic also applies to other intersections between boxes and rows/columns:
	// - a box can restrict cells in a column which intersects with it
	// - a row can restrict cells in a box which intersects with it
	// - a column can restrict cells in a box which intersects with it
	
	//-----------------------------------------------------------------------------------------

	// Sub-class to record details of a particular restriction found for a symbol. In the restricted cellset, the symbol 
	// must be in a cell which intersects with the restrictor cellset
	private static class Restriction {
		Symbol m_symbol;
		CellSet m_restrictorCellSet; 	
		CellSet m_restrictedCellSet;	  
		
		Restriction(Symbol symbol, CellSet restrictor, CellSet restricted) {
			m_symbol = symbol;
			m_restrictorCellSet = restrictor;
			m_restrictedCellSet = restricted;
		}
		
		// Which cells cannot be assigned to our symbol as a result of these restrictions ?
		Set<Cell> ruledOutCells() {
			return m_restrictedCellSet.getCellsNotIn(m_restrictorCellSet);
		}
		
		String getRepresentation() {
			return "Symbol " + m_symbol.getRepresentation() + " in " + m_restrictedCellSet.getRepresentation() + 
						" must be in a cell which intersects with " + m_restrictorCellSet.getRepresentation();
		}
	}

	//-----------------------------------------------------------------------------------------

	// See how the possible cells which a symbol may be assigned to within a 'restrictor' cellset can restrict what is possible in
	// other cellsets which intersect with this one. 
	private List<Restriction> findRestrictions(CellSetAssessment csa) {
		List<Restriction> lRestrictions = new ArrayList<>();		
		CellSet restrictorCellSet = csa.cellSet();
		
		for(Symbol symbol : csa.symbols()) {
			List<Cell> lCells = new ArrayList<>(csa.couldBeCellsForSymbol(symbol));
			if(lCells.size() > 1) {
				// We have a choice of cells that a symbol could appear in with this cell set. Work out how many separate intersecting columns, rows 
				// and boxes these cells appear in, other than our original cellset.
				Set<Box> intersectingBoxes = new HashSet<>();
				Set<Row> intersectingRows = new HashSet<>();
				Set<Column> intersectingColumns = new HashSet<>();
				for(Cell cell : lCells) {
					if(cell.box() != restrictorCellSet) intersectingBoxes.add(cell.box());
					if(cell.row() != restrictorCellSet) intersectingRows.add(cell.row());
					if(cell.column() != restrictorCellSet) intersectingColumns.add(cell.column());
				}

				// If the cells are all in the same intersecting column, row or box, then we've found a restriction we can apply to that 
				// intersecting cellset - the symbol cannot be in any of the cells in the intersecting column/row/box which are not also in our original cellset. 
				CellSet restrictedCellSet = null;
				if(intersectingBoxes.size() == 1) {
					restrictedCellSet = lCells.get(0).box();
				}				
				else if(intersectingRows.size() == 1) {
					restrictedCellSet = lCells.get(0).row();
				}
				else if(intersectingColumns.size() == 1) {
					restrictedCellSet = lCells.get(0).column();
				}
				
				if(restrictedCellSet != null) {
					lRestrictions.add(new Restriction(symbol, restrictorCellSet, restrictedCellSet));
				}
			}
		}
		
		return lRestrictions;
	}	

	boolean applyMethod(int stepNumber, List<String> actions) {
		boolean changedState = false;
		// Look for restrictions generated from each cellset (column, row, box) in turn. Apply them in turn to rule out
		// out cells until we've caused some sort of change (some restrictions may repeat rulings-out that have already 
		// been noted, so may not cause a state change). 
		for(CellSetAssessment csa : m_solver.cellSetAssessments()) {
			for(Restriction restriction : findRestrictions(csa)) {
				int changeCount = 0;
				for(Cell cell : restriction.ruledOutCells()) {
					changeCount += m_solver.spreadRulingOutImpact(cell, restriction.m_symbol, stepNumber);								
				}

				if(changeCount > 0) {
					changedState = true;
					actions.add(restriction.getRepresentation());
				}
				if(changedState) break;		// Only apply one restriction for each call of this method.			
			}
			if(changedState) break;		// Only apply one set restriction for each call of this method.			
		}
		return changedState;
	}
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

/**
 * Class which tries to find a subset of unassigned cells in a cellset which can only be assigned to a subset of the unassigned symbols,
 * allowing some other possibilities to be ruled out.
 */
class Method4 extends Method {
	
	Method4(Solver solver) {
		super(solver);
	}

	// We can sometimes work out that a subset of unassigned cells in a cellset must relate to a subset of the unsassigned symbols, they form a self-contained
	// unit. 
	//
	// For example, in working through a puzzle, we have reached a point where the cells in a column can have the following possible values:
	//
	//			Could be symbols		Notes
	// Row 1 	5 8 9
	// Row 2 	6						Already assigned
	// Row 3	7						Already assigned
	// Row 4	2 4
	// Row 5	3						Already assigned
	// Row 6	2 4 8
	// Row 7	2 4
	// Row 8	5 9
	// Row 9	1						Already assigned
	//
	// We can see from this that the symbols 5, 8 and 9 can only appear in the cells in rows 1, 6, and 8. So we have 3 symbols which can only appear in just 3 cells.
	// This means that those three cells must only contain 5, 8, and 9, and no other symbols. In particular, the cell in row 6 cannot be assigned to 2 or 4 (otherwise
	// we would be left with only two cells to assign three symbols to), so we can do some ruling out based on this (resulting in row 6 being assignable to the symbol 8).
	// 	
	// So in general, if we find that a set of n symbols can only appear in n cells in a cell set, we can rule out any other symbol from being assigned to any of those n cells.
	//
	// This method finds these corresponding sets of n symbols and cells in a cellset, and applies the restrictions they imply.
	
	//-----------------------------------------------------------------------------------------

	// Sub-class to record details of a particular restriction found in a cellset. In the restriction, the set of symbols can only be assigned to the 
	// cells in the set of cells.
	private static class Restriction {
		CellSet m_cellSet;	
		Set<Symbol> m_symbols;
		Set<Cell> m_cells;
		
		Restriction(CellSet cellSet, Set<Symbol> symbols, Set<Cell> cells) {
			m_cellSet = cellSet;	
			m_symbols = symbols;
			m_cells = cells;		
		}
		
		String getRepresentation() {
			return "Restriction for " + m_cellSet.getRepresentation() + ":" + 
						" the " + m_symbols.size() + " symbols {" + Symbol.symbolCollectionRepresentation(m_symbols) + "} are restricted to " + 
						" the " + m_cells.size() + " cells {" + Cell.cellCollectionRepresentation(m_cells) + "}"; 
		}
	}

	//-----------------------------------------------------------------------------------------

	// Find the restrictions for a particular cellset, where a set of n unassigned symbols can only be assigned to a set of n unassigned cells.
	// NB The code below looks for restrictions comprised of 2, 3 or 4 cells/symbols. Maybe look for a way to extend to more, probably by
	// recursion.	
	private List<Restriction> findRestrictions(CellSetAssessment csa) {
		// Find all the combinations of 2, 3 or 4 unassigned symbols in the cellset (using the ordinal method to avoid duplicates combinations
		// with a different cell ordering).
		List<Set<Symbol>> symbolCombinations = new ArrayList<>();		
		for(Symbol symbol1 : csa.symbols()) {
			if(csa.couldBeCellCount(symbol1) > 1) {
				Set<Symbol> combo1 = new LinkedHashSet<>(); combo1.add(symbol1);
				for(Symbol symbol2 : csa.symbols()) {
					if(symbol2.ordinal() > symbol1.ordinal()) {
						if(csa.couldBeCellCount(symbol2) > 1) {
							// We have a combination of two symbols to investigate ...
							Set<Symbol> combo2 = new LinkedHashSet<>(combo1); combo2.add(symbol2);
							symbolCombinations.add(combo2);							
							for(Symbol symbol3 : csa.symbols()) {
								if(symbol3.ordinal() > symbol2.ordinal()) {
									if(csa.couldBeCellCount(symbol3) > 1) {
										// We have a combination of three symbols to investigate ...
										Set<Symbol> combo3 = new LinkedHashSet<>(combo2); combo3.add(symbol3); 
										symbolCombinations.add(combo3);
										for(Symbol symbol4 : csa.symbols()) {
											if(symbol4.ordinal() > symbol3.ordinal()) {
												if(csa.couldBeCellCount(symbol4) > 1) {
													// We have a combination of four symbols to investigate ...
													Set<Symbol> combo4 = new LinkedHashSet<>(combo3); combo4.add(symbol4); 
													symbolCombinations.add(combo4);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}			
		}

		// For each unassigned symbol combination we've found, see how many cells these symbols can still potentially be
		// assigned to. If it's the same as the number of symbols in the combination, we've found a restriction.
		// NB Incorporating this in the loops above would be more efficient.
		List<Restriction> restrictions = new ArrayList<>();
		for(Set<Symbol> symbolCombination : symbolCombinations) {
			Set<Cell> cells = getSymbolCombinationCells(csa, symbolCombination);
			if(symbolCombination.size() == cells.size()) {
				Restriction r = new Restriction(csa.cellSet(), symbolCombination, cells);
				restrictions.add(r);
				//System.err.println("Found restriction: " + r.getRepresentation());
			}
		}		
		
		return restrictions;
	}

	// Get the set of cells which can still be assigned to this set of symbols.
	private Set<Cell> getSymbolCombinationCells(CellSetAssessment csa, Set<Symbol> symbolCombination) {
		Set<Cell> cells = new LinkedHashSet<>();	// Must be a set, only want to record each cell once.
		for(Symbol symbol : symbolCombination) {
			for(Cell cell : csa.couldBeCellsForSymbol(symbol)) {
				cells.add(cell);
			}
		}
				
		return cells;
	}

	boolean applyMethod(int stepNumber, List<String> actions) {
		boolean changedState = false;
		// Look for restricted sets of cells/symbols in each cellset, and apply them until one of them
		// causes a state change.
		for(CellSetAssessment csa : m_solver.cellSetAssessments()) {
			List<Restriction> lRestrictions = findRestrictions(csa);
			for(Restriction restriction : lRestrictions) {						
				changedState = processRestriction(csa, restriction, stepNumber);
				if(changedState) {
					actions.add(restriction.getRepresentation());
					break;
				}
			}
			if(changedState) break;
		}
		return changedState;
	}

	// Handle the processing of a specific restriction, rule out cell/symbol combinations not covered by the restriction
	private boolean processRestriction(CellSetAssessment csa, Restriction restriction, int stepNumber) {
		int stateChanges = 0;
		
		// Rule out other symbols for these cells
		for(Cell cell : restriction.m_cells) {
			Set<Symbol> ruledOutSymbols = csa.symbols().stream()
						.filter(s -> !restriction.m_symbols.contains(s))
						.collect(Collectors.toSet());
			for(Symbol symbol : ruledOutSymbols) {
				stateChanges += m_solver.spreadRulingOutImpact(cell, symbol, stepNumber);
			}
		}
		
		// Rule out other cells for these symbols
		for(Symbol symbol : restriction.m_symbols) {
			Set<Cell> ruledOutCells = csa.cellSet().cells().stream()
						.filter(c -> !restriction.m_cells.contains(c))
						.collect(Collectors.toSet());
			for(Cell cell : ruledOutCells) {
				stateChanges += m_solver.spreadRulingOutImpact(cell, symbol, stepNumber);
			}
		}

		boolean changedState = (stateChanges > 0);
		return changedState;
	}
}	
