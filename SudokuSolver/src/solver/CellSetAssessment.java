package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import grid.Cell;
import grid.CellSet;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

abstract class CellSetAssessment implements Comparable<CellSetAssessment> {
	
	private CellSet m_cellSet;	
	private Set<Symbol> m_assignedSymbols;
	private HashMap<Symbol, List<Cell>> m_couldBeCellsForSymbol;
	private int m_stepNumberOfLatestChange;

	public CellSetAssessment(CellSet cellSet, SymbolsToUse symbols) {
		m_cellSet = cellSet;
		m_assignedSymbols = new LinkedHashSet<>();
		
		m_couldBeCellsForSymbol = new HashMap<>();		
		for(Symbol symbol : symbols.getSymbolSet()) {
			m_couldBeCellsForSymbol.put(symbol, new ArrayList<Cell>());
		}
		
		m_stepNumberOfLatestChange = -1;
	}
	
	void addCell(Cell cell)	{
		for(List<Cell> lCells : m_couldBeCellsForSymbol.values()) {
			lCells.add(cell);
		}
	}

	int getItemNumber() {
		return m_cellSet.getItemNumber();
	}

	boolean contains(Cell cell) {
		return m_cellSet.containsCell(cell);
	}
	
	CellSet getCellSet() {
		return m_cellSet;
	}

	String getRepresentation() {
		return m_cellSet.getRepresentation();
	}

	String getOneBasedRepresentation() {
		return m_cellSet.getOneBasedRepresentation();
	}

	Set<Symbol> getSymbols() {
		return m_couldBeCellsForSymbol.keySet();
	}
		
	List<Cell> getCouldBeCellsForSymbol(Symbol symbol) {
		return new ArrayList<>(m_couldBeCellsForSymbol.get(symbol));
	}

	// If there is only one cell this symbol could be used for, return that cell
	Cell getOnlyCouldBeCellForSymbol(Symbol symbol) {
		Cell cell = null;
		if(m_couldBeCellsForSymbol.get(symbol).size() == 1) {
			cell = m_couldBeCellsForSymbol.get(symbol).get(0);
		}
		return cell;
	}
	
	private void setStepNumber(int n) {
		m_stepNumberOfLatestChange = n;
	}
	
	public int stepNumberOfLatestChange() { 
		return m_stepNumberOfLatestChange;
	}
	
	// ------------------------------
	// These three arise after an assignment has been made to a cell

	// This cell is no longer a possible cell for this symbol.
	int ruleOutCellForSymbol(Cell notThisCell, Symbol symbol, int stepNumber) {
		int changed = 0;
		List<Cell> lCouldBeCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		if(lCouldBeCellsForThisSymbol.contains(notThisCell)) {
			lCouldBeCellsForThisSymbol.remove(notThisCell);
			setStepNumber(stepNumber);
			changed++;
		}
		return changed;
	}
	
	// This cell can only apply to this symbol, rule out the cell for all other symbols
	int ruleOutCellForOtherSymbols(Cell assignmentCell, Symbol symbol, int stepNumber) {
		int changed = 0;
		for(Symbol otherSymbol : getSymbols()) {
			if(otherSymbol != symbol) {				
				changed += ruleOutCellForSymbol(assignmentCell, otherSymbol, stepNumber);
			}
		}		
		
		return changed;
	}
	
	// Only this cell can apply to this symbol, rule out the other cells for this symbol
	int ruleOutOtherCellsForSymbol(Cell assignmentCell, Symbol symbol, int stepNumber) {
		int changed = 0;
		List<Cell> lUnwantedCells = getCouldBeCellsForSymbol(symbol).stream()
				.filter(couldBeCell -> couldBeCell != assignmentCell)
				.collect(Collectors.toList());
				
		for(Cell unwantedCell : lUnwantedCells) {
			changed += ruleOutCellForSymbol(unwantedCell, symbol, stepNumber);
		}
		
		return changed;
	}
	
	// ------------------------------
	
	// Two below arise during combinations handling, not due to an assignment
	
	// These cells are the only ones possible for the symbol, rule any other cells out
	int ruleOutAllOtherCellsForSymbol(List<Cell> lCellsToKeep, Symbol symbol, int stepNumber) {
		int changed = 0;
		
		List<Cell> lUnwantedCells = getCouldBeCellsForSymbol(symbol).stream()
				.filter(couldBeCell -> !lCellsToKeep.contains(couldBeCell))
				.collect(Collectors.toList());
				
		for(Cell unwantedCell : lUnwantedCells) {
			changed += ruleOutCellForSymbol(unwantedCell, symbol, stepNumber);
		}
		
		return changed;
	}

	// These symbols are the only ones possible for this cell, rule this cell out for use in other symbols
	int ruleOutCellFromOtherSymbols(Cell cell, List<Symbol> lSymbols, int stepNumber) {
		int changed = 0;
		for(Symbol symbol : getSymbols()) {
			if(!lSymbols.contains(symbol)) {
				changed += ruleOutCellForSymbol(cell, symbol, stepNumber);
			}
		}
		return changed;
	}
	
	@Override
	public int compareTo(CellSetAssessment csa) {
	   // comparison logic goes here
		return this.getItemNumber() - csa.getItemNumber();
	}

	boolean symbolAlreadyAssigned(Symbol symbol) {
		return m_assignedSymbols.contains(symbol);
	}
	
	// A particular symbol has been assigned to a cell, so mark it as ruled-out for other cells in this set.
	void assignmentMade(Symbol symbol, Cell cell, int stepNumber) {
		// Add to the list of symbols in this set which are now assigned.
		if(!symbolAlreadyAssigned(symbol))
		{
			m_assignedSymbols.add(symbol);
			setStepNumber(stepNumber);
		}
		ruleOutOtherCellsForSymbol(cell, symbol, stepNumber);
		ruleOutCellForOtherSymbols(cell, symbol, stepNumber);
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
		
		for(Symbol symbol1 : getSymbols()) {
			List<Cell> lCells1 = getCouldBeCellsForSymbol(symbol1);
			if(lCells1.size() > 1) {

				for(Symbol symbol2 : getSymbols()) {
					if(symbol2.ordinal() > symbol1.ordinal()) {
						List<Cell> lCells2 = getCouldBeCellsForSymbol(symbol2);
						if(lCells2.size() > 1) {
							// We have a combination of two symbols to investigate ...
							List<Symbol> l2 = new ArrayList<>();
							l2.add(symbol1); l2.add(symbol2);
							lCombinations.add(l2);
							
							for(Symbol symbol3 : getSymbols()) {
								if(symbol3.ordinal() > symbol2.ordinal()) {
									List<Cell> lCells3 = getCouldBeCellsForSymbol(symbol3);
									if(lCells3.size() > 1) {
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);

										for(Symbol symbol4 : getSymbols()) {
											if(symbol4.ordinal() > symbol3.ordinal()) {
												List<Cell> lCells4 = getCouldBeCellsForSymbol(symbol4);
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

		System.err.println("Found " + lCombinations.size() + " symbol combinations for " + m_cellSet.getRepresentation());
		for(List<Symbol> lCombination : lCombinations) {
			List<Cell> lCellsForCombination = getSymbolCombinationCells(lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet) {
				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolCollectionToString(lCombination) + " covers cells " +  Cell.cellCollectionToString(lCellsForCombination));				
				SymbolSetRestriction restriction = new SymbolSetRestriction(m_cellSet, lCombination, lCellsForCombination);
				l.add(restriction);
			}
		}		
		
		return l;
	}

	private List<Cell> getSymbolCombinationCells(List<Symbol> lCombination) {
		Set<Cell> cells = new TreeSet<>();
		for(Symbol symbol : lCombination) {
			List<Cell> l = getCouldBeCellsForSymbol(symbol);
			for(Cell cell : l) {
				cells.add(cell);
			}
		}
				
		return new ArrayList<Cell>(cells);
	}

	String getSymbolAssignmentSummary() {
		StringBuilder sbSingleCell = new StringBuilder();
		StringBuilder sbMultiCell = new StringBuilder();
		List<Symbol> lSymbols = new ArrayList<>(getSymbols());
		Collections.sort(lSymbols);
				
		for(Symbol symbol : lSymbols) {
			List<Cell> lCells = getCouldBeCellsForSymbol(symbol);
			String cellListString = Cell.cellCollectionToString(lCells);
			
			if(lCells.size() == 1) {
				String markAsUnassigned = "";
				if(!m_assignedSymbols.contains(symbol)) {
					markAsUnassigned = "*";
				}
				sbSingleCell.append(symbol.getRepresentation() + ":" + cellListString + markAsUnassigned + " ");
			}
			else {
				sbMultiCell.append(symbol.getRepresentation() + ":[" + cellListString + "] ");				
			}
		}
		
		String returnString = "";
		if(sbMultiCell.toString().trim().length() > 0)
		{
			returnString += "Unresolved: " + sbMultiCell.toString().trim() + "   ";
		}
		
		if(sbSingleCell.toString().trim().length() > 0) {
			returnString += "Resolved: " + sbSingleCell.toString().trim() + "   ";			
		}
		
		return returnString.trim();
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
		Set<CellSet> set = new TreeSet<>();		// Tree set maintains sorting order, LinkedHashSet maintains insertion order ????	
		for(Cell cell : m_lCells) {
			set.add(cell.box());
			set.add(cell.row());
			set.add(cell.column());
		}
		return new ArrayList<CellSet>(set);		
	}
	
	String getRepresentation() {
		return "SymbolSetRestriction for " + m_cellSet.getOneBasedRepresentation() + " Symbols: " + Symbol.symbolCollectionToString(m_lSymbols) + ", Cells : " + Cell.cellCollectionToString(m_lCells); 
	}
}
