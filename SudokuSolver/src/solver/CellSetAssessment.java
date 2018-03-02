package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import grid.Cell;
import grid.CellSet;
import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

abstract class CellSetAssessment implements Comparable<CellSetAssessment> {
	
	private CellSet m_cellSet;
	Set<CellAssessment> m_lCellAssessments;		// Private, remove altogether and use list of cells from m_cellSet ????
	
	private HashMap<Symbol, Assignment> m_assignedSymbols;
	private HashMap<Symbol, List<Cell>> m_couldBeCellsForSymbol;

	public CellSetAssessment(CellSet cellSet, SymbolsToUse symbols) {
		m_cellSet = cellSet;
		m_lCellAssessments = new TreeSet<>();
		m_assignedSymbols = new HashMap<>();
		
		m_couldBeCellsForSymbol = new HashMap<>();		
		for(Symbol symbol : symbols.getSymbolSet()) {
			m_couldBeCellsForSymbol.put(symbol, new ArrayList<Cell>());
		}
	}
	
	void addCellAssessment(CellAssessment ca)	{
		m_lCellAssessments.add(ca);
		for(List<Cell> lCells : m_couldBeCellsForSymbol.values()) {
			lCells.add(ca.m_cell);
		}
	}

	int getItemNumber() {
		return m_cellSet.getItemNumber();
	}

	boolean contains(Cell cell) {
		return m_cellSet.containsCell(cell);
	}
	
	String getRepresentation() {
		return m_cellSet.getRepresentation();
	}

	Set<Symbol> getSymbols() {
		return m_couldBeCellsForSymbol.keySet();
	}
		
	List<Cell> getCouldBeCellsForSymbol(Symbol symbol) {
		return m_couldBeCellsForSymbol.get(symbol);
	}


	// ------------------------------
	// These three arise after an assignment has been made to a cell

	// This cell is no longer a possible cell for this symbol.
	int ruleOutCellForSymbol(Cell notThisCell, Symbol symbol) {
		int changed = 0;
		List<Cell> lCouldBeCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		if(lCouldBeCellsForThisSymbol.contains(notThisCell)) {
			lCouldBeCellsForThisSymbol.remove(notThisCell);
			changed++;
		}
		return changed;
	}
	
	// This cell can only apply to this symbol, rule out the cell for all other symbols
	int ruleOutCellForOtherSymbols(Cell assignmentCell, Symbol symbol) {
		int changed = 0;
		for(Symbol otherSymbol : m_couldBeCellsForSymbol.keySet()) {
			if(otherSymbol != symbol) {				
				changed += ruleOutCellForSymbol(assignmentCell, otherSymbol);
			}
		}		
		
		return changed;
	}
	
	// Only this cell can apply to this symbol, rule out the other cells for this symbol
	void ruleOutOtherCellsForSymbol(Cell assignmentCell, Symbol symbol) {
		List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.clear();
		lCellsForThisSymbol.add(assignmentCell);		
	}
	
	// ------------------------------
	
	// Two below arise during combinations handling, not due to an assignment
	
	// These cells are the only ones possible for the symbol, rule any other cells out
	int ruleOutAllOtherCellsForSymbol(List<Cell> lCellsToKeep, Symbol symbol) {
		int changed = 0;
		
		List<Cell> lUnwantedCells = m_couldBeCellsForSymbol.get(symbol).stream()
				.filter(couldBeCell -> !lCellsToKeep.contains(couldBeCell))
				.collect(Collectors.toList());
				
		for(Cell unwantedCell : lUnwantedCells) {
			changed += ruleOutCellForSymbol(unwantedCell, symbol);
		}
		
		return changed;
	}

	// These symbols are the only ones possible for this cell, rule this cell out for use in other symbols
	int ruleOutCellFromOtherSymbols(Cell cell, List<Symbol> lSymbols) {
		int changed = 0;
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet()) {
			if(!lSymbols.contains(symbol)) {
				changed += ruleOutCellForSymbol(cell, symbol);
			}
		}
		return changed;
	}

	// ------------------------------

	// These two arise when a symbol is restricted to the cells at the intersection of a specific row/column with a specific box. The symbol can be
	// ruled out for all the other cells in the two intersecting cell-sets
	
	boolean ruleOutSymbolOutsideBox(SymbolRestriction restriction) {
		boolean changedState = false;
		// For cells not in the restriction box, rule out the symbol.
		for(CellAssessment cell : m_lCellAssessments) {
			if(!restriction.m_box.containsCell(cell.m_cell)) {
				if(!cell.isRuledOut(restriction.m_symbol)) {
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.m_cell.getGridLocationString());				
					cell.ruleOut(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}
	
	boolean ruleOutSymbolOutsideRowOrColumn(SymbolRestriction restriction) {
		boolean changedState = false;
		// For cells not in the restriction row/column, rule out the symbol.
		for(CellAssessment cell : m_lCellAssessments) {
			if(!restriction.m_rowOrColumn.containsCell(cell.m_cell)) {
				if(!cell.isRuledOut(restriction.m_symbol)) {
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.m_cell.getGridLocationString());				
					cell.ruleOut(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}

	@Override
	public int compareTo(CellSetAssessment csa) {
	   // comparison logic goes here
		return this.getItemNumber() - csa.getItemNumber();
	}

	boolean symbolAlreadyAssigned(Symbol symbol) {
		return m_assignedSymbols.containsKey(symbol);
	}
	
	// A particular symbol has been assigned to a cell, so mark it as ruled-out for other cells in this set.
	void assignmentMade(Assignment assignment, Cell cell) {
		// Add to the list of symbols in this set which are now assigned.
		m_assignedSymbols.put(assignment.getSymbol(), assignment);
		ruleOutOtherCellsForSymbol(cell, assignment.getSymbol());
		ruleOutCellForOtherSymbols(cell, assignment.getSymbol());
	}

	Assignment hasAssignmentAvailable(int stepNumber) {
		Assignment assignableCell = null;
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet()) {
			if(!m_assignedSymbols.containsKey(symbol)) {
				List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
				if(lCells.size() == 1) {
					assignableCell = new Assignment(lCells.get(0), symbol, AssignmentMethod.AutomatedDeduction, "Only cell for symbol in " + m_cellSet.getRepresentation(), stepNumber);
					break;
				}
			}
		}
		
		return assignableCell;
	}

	boolean isComplete() {
		return m_assignedSymbols.size() == m_cellSet.size();
	}
	
	// Goes up to combinations of 4 - how to generalise to n ?
	List<SymbolSetRestriction> findRestrictedSymbolSets() {
		List<SymbolSetRestriction> l = new ArrayList<>();
		// Generate combinations of 2, 3 and 4 unassigned symbols. If the combination has n symbols and between them these can only
		// be placed in n cells, then we have a restricted symbol set.
		
		List<List<Symbol>> lCombinations = new ArrayList<>();
		
		for(Symbol symbol1 : m_couldBeCellsForSymbol.keySet()) {
			List<Cell> lCells1 = m_couldBeCellsForSymbol.get(symbol1);
			if(lCells1.size() > 1) {

				for(Symbol symbol2 : m_couldBeCellsForSymbol.keySet()) {
					if(symbol2.ordinal() > symbol1.ordinal()) {
						List<Cell> lCells2 = m_couldBeCellsForSymbol.get(symbol2);
						if(lCells2.size() > 1) {
							// We have a combination of two symbols to investigate ...
							List<Symbol> l2 = new ArrayList<>();
							l2.add(symbol1); l2.add(symbol2);
							lCombinations.add(l2);
							
							for(Symbol symbol3 : m_couldBeCellsForSymbol.keySet()) {
								if(symbol3.ordinal() > symbol2.ordinal()) {
									List<Cell> lCells3 = m_couldBeCellsForSymbol.get(symbol3);
									if(lCells3.size() > 1) {
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);

										for(Symbol symbol4 : m_couldBeCellsForSymbol.keySet()) {
											if(symbol4.ordinal() > symbol3.ordinal()) {
												List<Cell> lCells4 = m_couldBeCellsForSymbol.get(symbol4);
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

		System.err.println("Found " + lCombinations.size() + " symbol combinations for set " + m_cellSet.getRepresentation());
		for(List<Symbol> lCombination : lCombinations) {
			List<Cell> lCellsForCombination = getSymbolCombinationCells(lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet) {
				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolListToString(lCombination) + " covers cells " +  Cell.cellListToString(lCellsForCombination));
				
				SymbolSetRestriction restriction = new SymbolSetRestriction(m_cellSet, lCombination, lCellsForCombination);
				l.add(restriction);
			}
		}		
		
		return l;
	}

	private List<Cell> getSymbolCombinationCells(List<Symbol> lCombination) {
		Set<Cell> cells = new TreeSet<>();
		for(Symbol symbol : lCombination) {
			List<Cell> l = m_couldBeCellsForSymbol.get(symbol);
			for(Cell cell : l) {
				cells.add(cell);
			}
		}
				
		return new ArrayList<Cell>(cells);
	}

	static boolean compareCellLists(List<Cell> lCells1, List<Cell> lCells2) {
		boolean same = true;

		if(lCells1.size() != lCells2.size()) return false;
		
		List<Cell> l1 = new ArrayList<>(lCells1);
		List<Cell> l2 = new ArrayList<>(lCells2);
		
		Collections.sort(l1);
		Collections.sort(l2);

		for(int n=0; n < l1.size(); n++) {
			if(l1.get(n) != l2.get(n)) {
				same = false;
				break;
			}
		}
		
		return same;
	}

	static boolean compareSymbolLists(List<Symbol> lSymbols1, List<Symbol> lSymbols2)
	{
		boolean same = true;

		if(lSymbols1.size() != lSymbols2.size()) return false;
		
		List<Symbol> l1 = new ArrayList<>(lSymbols1);
		List<Symbol> l2 = new ArrayList<>(lSymbols2);
		
		Collections.sort(l1);
		Collections.sort(l2);

		for(int n=0; n < l1.size(); n++) {
			if(l1.get(n) != l2.get(n)) {
				same = false;
				break;
			}
		}
		
		return same;
	}

	String getSymbolAssignmentSummary() {
		StringBuilder sbSingleCell = new StringBuilder();
		StringBuilder sbMultiCell = new StringBuilder();
		List<Symbol> lSymbols = new ArrayList<>(m_couldBeCellsForSymbol.keySet());
		Collections.sort(lSymbols);
				
		for(Symbol symbol : lSymbols) {
			List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
			String cellListString = Cell.cellListToString(lCells);
			
			if(lCells.size() == 1) {
				String markAsUnassigned = "";
				if(!m_assignedSymbols.containsKey(symbol)) {
					markAsUnassigned = "*";
				}
				sbSingleCell.append(symbol.getRepresentation() + ":" + cellListString + markAsUnassigned + " ");
			}
			else {
				sbMultiCell.append(symbol.getRepresentation() + ":[" + cellListString + "] ");				
			}
		}
		
		
		return "Unresolved: " + sbMultiCell.toString().trim() + "   Resolved: " + sbSingleCell.toString().trim();
	}
}
		
//Paired symbols in a cell set which can only exist in a subset of cells. The two lists will be the same length.  
class SymbolSetRestriction {
	CellSet m_cellSet;	
	List<Symbol> m_lSymbols;
	List<Cell> m_lCells;
	
	SymbolSetRestriction(CellSet cellSet, List<Symbol> lSymbols, List<Cell> lCells) {
		m_cellSet = cellSet;	
		m_lSymbols = lSymbols;
		m_lCells = lCells;		
	}
	
	List<CellSet> getAffectedCellSets() {
		Set<CellSet> set = new TreeSet<>();	
		for(Cell cell : m_lCells) {
			set.add(cell.getBox());
			set.add(cell.getRow());
			set.add(cell.getColumn());
		}
		return new ArrayList<CellSet>(set);		
	}
}
