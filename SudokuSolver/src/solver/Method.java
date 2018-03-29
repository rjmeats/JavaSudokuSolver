package solver;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collection;

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
	 * Every class extending the Method class provides applyMethod to try to make some progress on solving the Sudoku.
	 *  
	 * @param stepNumber
	 * @param actions
	 * @return
	 * @throws IllegalAssignmentException
	 */
	abstract boolean applyMethod(int stepNumber, List<String> actions) throws IllegalAssignmentException;
}

// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------

/**
 * Class which tries to find symbols which can be assigned only to one specific cell in a cellset (column, row or box) now.
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
 * Class which tries to find cells which can only have one specific symbol assigned to them now.
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
	// For the top-right box (Box 3), the 7 must only be in Row 2. 
	// Consequently we can rule out 7 from being any of the cells in Row 2 which are outside Box 3
	// So the cells marked with a * cannot be a 7, and we can apply this 7-is-ruled-out restriction to those cells.
	
	boolean applyMethod(int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellSetAssessment csa : m_solver.cellSetAssessments()) {
			List<Method3Restriction> lRestrictions = findMethod3Restrictions(csa);
			changedState = applyMethod3Restrictions(lRestrictions, stepNumber, actions);			
			if(changedState) break;
		}
		return changedState;
	}
	
	private static class Method3Restriction {
		Symbol m_symbol;
		CellSet m_restrictorCellSet;
		CellSet m_restrictedCellSet;
		Set<Cell> m_restrictedCells;
		
		Method3Restriction(Symbol symbol, CellSet restrictor, CellSet restricted) {
			m_symbol = symbol;
			m_restrictorCellSet = restrictor;
			m_restrictedCellSet = restricted;
			m_restrictedCells = m_restrictedCellSet.getCellsNotIn(m_restrictorCellSet);
		}
		
		String getRepresentation() {
			return "Symbol " + m_symbol.getRepresentation() + " in " + m_restrictedCellSet.getRepresentation() + 
						" must be in " + m_restrictorCellSet.getRepresentation();
		}
	}

	private List<Method3Restriction> findMethod3Restrictions(CellSetAssessment csa) {
		
		CellSet thisCellSet = csa.cellSet();
		List<Method3Restriction> lRestrictions = new ArrayList<>();		
		
		for(Symbol symbol : csa.symbols()) {
			List<Cell> lCells = new ArrayList<>(csa.couldBeCellsForSymbol(symbol));
			if(lCells.size() > 1) {
				Set<Box> boxSet = new HashSet<>();
				Set<Row> rowSet = new HashSet<>();
				Set<Column> columnSet = new HashSet<>();
				for(Cell cell : lCells) {
					boxSet.add(cell.box());
					rowSet.add(cell.row());
					columnSet.add(cell.column());
				}

				CellSet restrictedCellSet = null;
				if(boxSet.size() == 1) {
					Box box = lCells.get(0).box();
					if(box != thisCellSet) {
						restrictedCellSet = box;
					}
				}
				
				if(rowSet.size() == 1) {
					Row row = lCells.get(0).row();
					if(row != thisCellSet) {
						restrictedCellSet = row;
					}
				}
				
				if(columnSet.size() == 1) {
					Column column = lCells.get(0).column();
					if(column != thisCellSet) {
						restrictedCellSet = column;
					}
				}				

				if(restrictedCellSet != null) {
					lRestrictions.add(new Method3Restriction(symbol, thisCellSet, restrictedCellSet));
				}
			}
		}
		
		return lRestrictions;
	}	
		
	private boolean applyMethod3Restrictions(List<Method3Restriction> lRestrictions, int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(Method3Restriction restriction : lRestrictions) {
			int changeCount = 0;
			for(Cell cell : restriction.m_restrictedCells) {
				changeCount += m_solver.spreadRulingOutImpact(cell, restriction.m_symbol, stepNumber);								
			}

			if(changeCount > 0) {
				changedState = true;
				actions.add(restriction.getRepresentation());
				break;
			}
		}

		return changedState;
	}
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

class Method4 extends Method {
	
	Method4(Solver solver) {
		super(solver);
	}
	
	// Where n symbols in a row/column/box can only be assigned to the same n cells, then these cells can't be assigned to any other symbols.
	boolean applyMethod(int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(CellSetAssessment set : m_solver.cellSetAssessments()) {
			List<SymbolSetRestriction> lRestrictedSymbolSets = findRestrictedSymbolSets(set);
			if(lRestrictedSymbolSets != null) {
				for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {						
					for(Cell cell : symbolSetRestriction.m_lCells) {
						CellAssessment ca = m_solver.assessmentForCell(cell);
						boolean causedStateChange = (ca.ruleOutAllSymbolsExcept(symbolSetRestriction.m_lSymbols, stepNumber) > 0);
						if(causedStateChange) {
							stateChanges++;
							String s = "Restriction1: " + symbolSetRestriction.getRepresentation();
							actions.add(s);
							break;
						}
					}
					if(stateChanges > 0) break;
				}
	
				if(stateChanges > 0) break;
				for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {
					for(Symbol symbol : symbolSetRestriction.m_lSymbols) {
						CellSetAssessment cseta = m_solver.assessmentForCellSet(symbolSetRestriction.m_cellSet);
						int causedChange = cseta.ruleOutAllOtherCellsForSymbol(symbolSetRestriction.m_lCells, symbol, stepNumber);
						if(causedChange > 0) {
							stateChanges++;
							String s = "Restriction2: " + symbolSetRestriction.getRepresentation();
							actions.add(s);
//							break;
						}
					}
					
//					if(stateChanges > 0) break;
					List<CellSet> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
					for(CellSet cset : lAffectedCellSets) {
						CellSetAssessment cseta = m_solver.assessmentForCellSet(cset);
						for(Cell cell : symbolSetRestriction.m_lCells) {
							int causedChange = cseta.ruleOutCellFromOtherSymbols(cell, symbolSetRestriction.m_lSymbols, stepNumber);
							if(causedChange > 0) {
								stateChanges++;
								String s = "Restriction3: " + symbolSetRestriction.getRepresentation();
								actions.add(s);
//								break;
							}
						}
					}
//					if(stateChanges > 0) break;
				}
//				if(stateChanges > 0) break;
			}
			if(stateChanges > 0) break;
		}
		
		changedState = (stateChanges > 0);
		return changedState;
	}
	
	// Goes up to combinations of 4 - how to generalise to n ?
	private List<SymbolSetRestriction> findRestrictedSymbolSets(CellSetAssessment csa) {
		List<SymbolSetRestriction> l = new ArrayList<>();
		// Generate combinations of 2, 3 and 4 unassigned symbols. If the combination has n symbols and between them these can only
		// be placed in n cells, then we have a restricted symbol set.
		
		List<List<Symbol>> lCombinations = new ArrayList<>();
		
		for(Symbol symbol1 : csa.symbols()) {
			Collection<Cell> lCells1 = csa.couldBeCellsForSymbol(symbol1);
			if(lCells1.size() > 1) {

				for(Symbol symbol2 : csa.symbols()) {
					if(symbol2.ordinal() > symbol1.ordinal()) {
						Collection<Cell> lCells2 = csa.couldBeCellsForSymbol(symbol2);
						if(lCells2.size() > 1) {
							// We have a combination of two symbols to investigate ...
							List<Symbol> l2 = new ArrayList<>();
							l2.add(symbol1); l2.add(symbol2);
							lCombinations.add(l2);
							
							for(Symbol symbol3 : csa.symbols()) {
								if(symbol3.ordinal() > symbol2.ordinal()) {
									Collection<Cell> lCells3 = csa.couldBeCellsForSymbol(symbol3);
									if(lCells3.size() > 1) {
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);

										for(Symbol symbol4 : csa.symbols()) {
											if(symbol4.ordinal() > symbol3.ordinal()) {
												Collection<Cell> lCells4 = csa.couldBeCellsForSymbol(symbol4);
												if(lCells4.size() > 1) {
													// We have a combination of four symbols to investigate ...
													List<Symbol> l4 = new ArrayList<>(l3); l4.add(symbol4); 
													lCombinations.add(l4);													
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

//		System.err.println("Found " + lCombinations.size() + " symbol combinations for " + csa.getCellSet().getRepresentation());
		for(List<Symbol> lCombination : lCombinations) {
			List<Cell> lCellsForCombination = getSymbolCombinationCells(csa, lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet) {
//				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolCollectionToString(lCombination) + " covers cells " +  Cell.cellCollectionToString(lCellsForCombination));				
				SymbolSetRestriction restriction = new SymbolSetRestriction(csa.cellSet(), lCombination, lCellsForCombination);
				l.add(restriction);
			}
		}		
		
		return l;
	}

	private List<Cell> getSymbolCombinationCells(CellSetAssessment csa, List<Symbol> lCombination) {
		Set<Cell> cells = new TreeSet<>();
		for(Symbol symbol : lCombination) {
			Collection<Cell> l = csa.couldBeCellsForSymbol(symbol);
			for(Cell cell : l) {
				cells.add(cell);
			}
		}
				
		return new ArrayList<Cell>(cells);
	}

	//Paired symbols in a cell set which can only exist in a subset of cells. The two lists will be the same length.  
	private class SymbolSetRestriction {
		CellSet m_cellSet;	
		List<Symbol> m_lSymbols;
		List<Cell> m_lCells;
		
		SymbolSetRestriction(CellSet cellSet, List<Symbol> lSymbols, List<Cell> lCells) {
			m_cellSet = cellSet;	
			m_lSymbols = lSymbols;
			m_lCells = lCells;		
		}
		
		List<CellSet> getAffectedCellSets() {
			Set<CellSet> set = new TreeSet<>();		// Tree set maintains sorting order, LinkedHashSet maintains insertion order ????	
			for(Cell cell : m_lCells) {
				set.add(cell.box());			// Invokes compare, so box, row, column need to implement comparable to do the ordering of CellSets  
				set.add(cell.row());
				set.add(cell.column());
			}
			return new ArrayList<CellSet>(set);		
		}
		
		String getRepresentation() {
			return "SymbolSetRestriction for " + m_cellSet.getRepresentation() + " Symbols: " + Symbol.symbolCollectionRepresentation(m_lSymbols) + ", Cells : " + Cell.cellCollectionRepresentation(m_lCells); 
		}
	}
}	
