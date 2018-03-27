package solver;

import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.stream.Collectors;

import grid.Cell;
import grid.CellSet;
import grid.Symbol;
import grid.Symbols;

/**
 * Class to record the ongoing assessment of what symbol/cell assignments are possible for a particular cellset (Column, Row, Box) in a grid.
 */

class CellSetAssessment implements Comparable<CellSetAssessment> {
	
	// Which cellset in the grid this assessment relates to
	private CellSet m_cellSet;
	
	// Which symbols are to be assigned to the cells in the cellset
	private Symbols m_symbols;
	
	// Keep track of which symbols have already been assigned to a cell, and which cells in the cellset 
	// could still be assigned to each of the symbols. 
	private Set<Symbol> m_assignedSymbols;
	private HashMap<Symbol, Set<Cell>> m_couldBeCellsForSymbol;
	
	// Which step in attempting to solve a Sudoku last caused a change to the assessments for this cellset as a whole and 
	// for each individual symbol. For info/diagnostic use. 
	private int m_stepNumberOfLatestChange;
	private HashMap<Symbol, Integer> m_stepNumberOfLatestChangeForSymbol;

	CellSetAssessment(CellSet cellSet, Symbols symbols) {
		m_cellSet = cellSet;
		m_symbols = symbols;

		// No symbols assigned yet.
		m_assignedSymbols = new LinkedHashSet<>();
		
		// Initially any of the cells in the cellset could be assigned to any symbol. 
		m_couldBeCellsForSymbol = new HashMap<>();
		for(Symbol symbol : m_symbols.getSymbolSet()) {
			m_couldBeCellsForSymbol.put(symbol, new LinkedHashSet<>(m_cellSet.cells()));
		}

		setStepNumber(-1);
		m_stepNumberOfLatestChangeForSymbol = new HashMap<>();
		for(Symbol symbol : m_symbols.getSymbolSet()) {
			setStepNumberOfLatestChangeForSymbol(symbol, -1);
		}
	}
	
	CellSet cellSet() {
		return m_cellSet;
	}

	Set<Symbol> symbols() {
		return m_symbols.getSymbolSet();
	}
		
	boolean contains(Cell cell) {
		return m_cellSet.containsCell(cell);
	}
	
	String getRepresentation() {
		return m_cellSet.getRepresentation();
	}

	// Implements Comparable, for ordering
	public int compareTo(CellSetAssessment csa) {
		return m_cellSet.compareTo(csa.m_cellSet);
	}

	// -----------------------------------------------------------------------------------------
	
	// Various access methods to check on what symbols already have a cell assigned, and what the could-be cells are for a symbol.
	
	boolean symbolAlreadyAssigned(Symbol symbol) {
		return m_assignedSymbols.contains(symbol);
	}
	
	Set<Cell> getCouldBeCellsForSymbol(Symbol symbol) {
		return new LinkedHashSet<>(m_couldBeCellsForSymbol.get(symbol));
	}

	/**
	 * Is there only one cell possible for this symbol now ? If so return it.
	 * 
	 * @param symbol Symbol whose cells are being checked
	 * @return The only possible cell for the symbol, if we've reached that point, otherwise null, indicating multiple possibilities still exist. 
	 */	
	Cell getOnlyCouldBeCellForSymbol(Symbol symbol) {
		Set<Cell> cells = m_couldBeCellsForSymbol.get(symbol);
		if (cells.size() == 1) {
			return cells.stream().findFirst().get();	// Functional way of getting first (only) item in a set
		}
		else
			return null;
	}

	/**
	 * Have all symbols had a cell assigned to them for this cellset ?
	 *  
	 * @return True if all symbols have a cell assigned
	 */
	boolean isComplete() {
		return m_assignedSymbols.size() == m_cellSet.size();
	}

	// -----------------------------------------------------------------------------------------
	
	// Recording/accessing the step number of the most recent change to the assessments for this cellset
		
	private void setStepNumber(int n) {
		m_stepNumberOfLatestChange = n;
	}
	
	int stepNumberOfLatestChange() { 
		return m_stepNumberOfLatestChange;
	}
	
	private void setStepNumberOfLatestChangeForSymbol(Symbol symbol, int n) {
		m_stepNumberOfLatestChangeForSymbol.put(symbol, n);
	}
	
	int stepNumberOfLatestChangeForSymbol(Symbol symbol) {
		return m_stepNumberOfLatestChangeForSymbol.get(symbol);
	}

	// -----------------------------------------------------------------------------------------
	
	// Methods to be called when an assignment has been made to a cell in this cellset, or when it is 
	// possible to rule out certain cell/symbol assignments. Update the could-be cell collection for
	// the relevant symbol and record the step number at which the change happened.
	//
	// Some of these methods can be called to rule out an option that is already ruled out, and so they 
	// return an indicator of whether the assessment actually changed or not, to allow diagnostics to be 
	// produced of the effect of each solution step.

	/**
	 * Records that a cell cannot be assigned to the specified symbol.
	 *  
	 * @param notThisCell The cell to rule out
	 * @param symbol The symbol which the cell cannot be assigned to
	 * @param stepNumber What is the current step number ?
	 * @return A count of how many cells have been ruled out by this call which weren't already ruled out.
	 */
	int ruleOutCellForSymbol(Cell notThisCell, Symbol symbol, int stepNumber) {
		int changed = 0;
		Set<Cell> couldBeCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		if(couldBeCellsForThisSymbol.contains(notThisCell)) {
			couldBeCellsForThisSymbol.remove(notThisCell);
			setStepNumber(stepNumber);
			setStepNumberOfLatestChangeForSymbol(symbol, stepNumber);
			changed++;
		}
		return changed;
	}

	/**
	 * Records that only the specified cells are possible for the symbol, no others.
	 *  
	 * @param stillPossibleCells Cells which could still be assigned to the symbol
	 * @param symbol Symbol which the cells can be assigned to
	 * @param stepNumber What is the current step number ?
	 * @return A count of how many cells have been ruled out by this call which weren't already ruled out.
	 */
	int ruleOutAllOtherCellsForSymbol(Collection<Cell> stillPossibleCells, Symbol symbol, int stepNumber) {
		int changed = 0;
		
		// See which cells are no longer possible, not being in the collection passed in. Functional approach.
		Set<Cell> noLongerPossibleCells = getCouldBeCellsForSymbol(symbol).stream()
				.filter(couldBeCell -> !stillPossibleCells.contains(couldBeCell))
				.collect(Collectors.toSet());
				
		for(Cell notThisCell : noLongerPossibleCells) {
			changed += ruleOutCellForSymbol(notThisCell, symbol, stepNumber);
		}
		
		return changed;
	}

	/**
	 * Records that only the cell can only be assigned to one of the specified symbols, no others.
	 *  
	 * @param cell Cell which the symbols can be assigned to
	 * @param stillPossibleSymbols Symbols which the cell could still be assigned to
	 * @param stepNumber What is the current step number ?
	 * @return A count of how many cells have been ruled out by this call which weren't already ruled out.
	 */
	int ruleOutCellFromOtherSymbols(Cell cell, Collection<Symbol> stillPossibleSymbols, int stepNumber) {
		int changed = 0;
		for(Symbol symbol : symbols()) {
			if(!stillPossibleSymbols.contains(symbol)) {
				changed += ruleOutCellForSymbol(cell, symbol, stepNumber);
			}
		}
		return changed;
	}
	
	/**
	 * To be called when a cell assignment has been made for a symbol.
	 *   
	 * @param symbol The symbol that has been assigned
	 * @param cell The cell that has been assigned
	 * @param stepNumber What is the current step number ?
	 */
	void assignmentMade(Symbol symbol, Cell cell, int stepNumber) {
		// Add to the list of symbols in this set which are now assigned.
		if(!symbolAlreadyAssigned(symbol)) {
			m_assignedSymbols.add(symbol);
			setStepNumber(stepNumber);
			setStepNumberOfLatestChangeForSymbol(symbol, stepNumber);
		}
		
		// And rule out using any other cell for this symbol, and using this cell with any other symbols  
		ruleOutOtherCellsForSymbol(cell, symbol, stepNumber);
		ruleOutCellForOtherSymbols(cell, symbol, stepNumber);
	}

	// This cell can only apply to this symbol, rule out the cell for all other symbols
	private int ruleOutCellForOtherSymbols(Cell cell, Symbol symbol, int stepNumber) {
		int changed = 0;
		for(Symbol otherSymbol : symbols()) {
			if(otherSymbol != symbol) {				
				changed += ruleOutCellForSymbol(cell, otherSymbol, stepNumber);
			}
		}		
		
		return changed;
	}
	
	// Only this cell can apply to this symbol, rule out the other cells for this symbol. Use the
	// cell-by-cell ruling out approach rather than just clearing out the set and adding the one 
	// assignment cell back in, to keep returned count and step numbering correct.
	private int ruleOutOtherCellsForSymbol(Cell assignmentCell, Symbol symbol, int stepNumber) {
		int changed = 0;
		Set<Cell> lUnwantedCells = getCouldBeCellsForSymbol(symbol).stream()
				.filter(couldBeCell -> couldBeCell != assignmentCell)
				.collect(Collectors.toSet());
				
		for(Cell unwantedCell : lUnwantedCells) {
			changed += ruleOutCellForSymbol(unwantedCell, symbol, stepNumber);
		}
		
		return changed;
	}
	
	// -----------------------------------------------------------------------------------------

	// Diagnostics to list the cells which could still be assigned to this symbol.
	String couldBeCellsRepresentation(Symbol symbol) {
		return Cell.cellCollectionRepresentation(getCouldBeCellsForSymbol(symbol));
	}	
}
