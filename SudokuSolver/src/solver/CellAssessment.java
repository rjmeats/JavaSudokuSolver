package solver;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import grid.Cell;
import grid.Symbol;
import grid.Symbols;

/**
 * Class to record the ongoing assessment of what symbol assignments are possible for a particular cell in a grid.
 */

class CellAssessment {

	// Which cell in the grid this assessment relates to
	private Cell m_cell;
	
	// Related assessments for the column, row and box which this cell appears in  
	private ColumnAssessment m_columnAssessment;
	private RowAssessment m_rowAssessment;
	private BoxAssessment m_boxAssessment;
	private Set<CellSetAssessment> m_cellSetAssessments;	// Combines the column, row and box assessments into a Set

	// Keep track of which symbols could still be assigned to this cell, and which have been ruled out. 
	private Set<Symbol> m_couldBeSymbolsSet;
	private Set<Symbol> m_ruledOutSymbolsSet;

	// Which step in attempting to solve a Sudoku last caused a change to the assessments for this cell. For info/diagnostic use. 
	private int m_stepNumberOfLatestChange;

	CellAssessment(Cell cell, RowAssessment rowAssessment, ColumnAssessment columnAssessment, BoxAssessment boxAssessment, Symbols symbols) {
		m_cell = cell;
		
		m_columnAssessment = columnAssessment;
		m_rowAssessment = rowAssessment;
		m_boxAssessment = boxAssessment;
		
		// Combine the column, row, and box assessments relating to this cell into a collection  
		m_cellSetAssessments = new LinkedHashSet<>();
		m_cellSetAssessments.add(columnAssessment);
		m_cellSetAssessments.add(rowAssessment);
		m_cellSetAssessments.add(boxAssessment);

		m_couldBeSymbolsSet = new LinkedHashSet<>(symbols.getSymbolSet());		// Everything's possible
		m_ruledOutSymbolsSet = new LinkedHashSet<>();							// Nothing ruled out yet
		
		m_stepNumberOfLatestChange = -1;
	}

	Cell cell() 							{ return m_cell; }
	RowAssessment rowAssessment() 			{ return m_rowAssessment; }
	ColumnAssessment columnAssessment() 	{ return m_columnAssessment; }
	BoxAssessment boxAssessment() 			{ return m_boxAssessment; }
	
	Set<CellSetAssessment> cellSetAssessments() {
		return new LinkedHashSet<>(m_cellSetAssessments);
	}

	// -----------------------------------------------------------------------------------------
	
	// Various access methods to check on what symbols are already assigned, could-be's or are ruled out for this cell.
	
	boolean isAssigned() {
		return m_cell.isAssigned();
	}
	
	boolean couldBe(Symbol symbol) {
		return m_couldBeSymbolsSet.contains(symbol);
	}

	boolean isRuledOut(Symbol symbol) {
		return m_ruledOutSymbolsSet.contains(symbol);
	}
		
	int couldBeCount() {
		return m_couldBeSymbolsSet.size();
	}

	List<Symbol> couldBeSymbols() {
		return new ArrayList<>(m_couldBeSymbolsSet);
	}

	// -----------------------------------------------------------------------------------------
	
	// Recording/accessing the step number of the most recent change to the assessments for this cell
		
	private void setStepNumber(int n) {
		m_stepNumberOfLatestChange = n;
	}
	
	int stepNumberOfLatestChange() { 
		return m_stepNumberOfLatestChange;
	}
	
	// -----------------------------------------------------------------------------------------
	
	// Methods to be called when an assignment has been made to this cell, or when it is possible to 
	// rule out certain symbol assignments. Update the could-be and ruled out collections and record
	// the step number at which the change happened. Some of these methods can be called to rule out
	// an option that is already ruled out, and so they return an indicator of whether the assessment
	// actually changed or not, to allow diagnostics to be produced of the effect of each solution step.

	/**
	 * Called when we can rule out applying a particular symbol to a cell.
	 *  
	 * @param symbol The symbol to rule out
	 * @param stepNumber What is the current step number ?
	 * @return A count of how many symbols have been ruled out by this call which weren't already ruled out.
	 */
	int ruleOutSymbol(Symbol symbol, int stepNumber) {
		int changeCount = 0;
		if(!isRuledOut(symbol)) {
			m_ruledOutSymbolsSet.add(symbol);
			m_couldBeSymbolsSet.remove(symbol);
			setStepNumber(stepNumber);
			changeCount++;
		}		
		return changeCount;
	}
	
	/**
	 * Called when we can rule out all symbols for a cell except for a specified set. 
	 * 
	 * (NB Symbols in the specified set might have been already ruled out for other reasons.) 
	 *  
	 * @param stillCouldBeSymbols A collection (list or set) of the symbols which the called assesses can still apply to this cell.
	 * @param stepNumber What is the current step number ?
	 * @return A count of how many symbols have been ruled out by this call which weren't already ruled out.
	 */
	int ruleOutAllSymbolsExcept(Collection<Symbol> stillCouldBeSymbols, int stepNumber) {
		// Go through the current could-be symbols, and create a list of those which are not in the
		// collection of symbols passed in, and so are no longer could-be candidates. Uses a Functional approach.
		Set<Symbol> lNoLongerPossibleSymbols = m_couldBeSymbolsSet.stream()
				.filter(couldBeSymbol -> !stillCouldBeSymbols.contains(couldBeSymbol))
				.collect(Collectors.toSet());
		
		// Rule out the symbols which are no longer possible.
		int changeCount = 0;		
		for(Symbol noLongerPossibleSymbol : lNoLongerPossibleSymbols) {
			changeCount += ruleOutSymbol(noLongerPossibleSymbol, stepNumber);
		}
		
		return changeCount;
	}

	/**
	 * To be called when an assignment has been made to a cell, to allow the assessments to be finalised.
	 *   
	 * @param symbol What symbol has been assigned to the cell
	 * @param stepNumber What is the current step number ?
	 */
	void assignmentMade(Symbol symbol, int stepNumber) {
		
		// We can now rule out all symbols except this one.
		Set<Symbol> l = new LinkedHashSet<>();
		l.add(symbol);
		ruleOutAllSymbolsExcept(l, stepNumber);
	}
	
	// -----------------------------------------------------------------------------------------

	// Diagnostics to list the symbols which could still be assigned to this cell.
	String couldBeSymbolsRepresentation() {
		return Symbol.symbolCollectionRepresentation(m_couldBeSymbolsSet);
	}	
}