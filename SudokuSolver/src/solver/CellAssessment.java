package solver;

import java.util.Set;


import java.util.stream.Collectors;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

import grid.Cell;
import grid.Symbol;
import puzzle.SymbolsToUse;

public class CellAssessment implements Comparable<CellAssessment> {

	private Cell m_cell;
	
	private RowAssessment m_rowAssessment;
	private ColumnAssessment m_columnAssessment;
	private BoxAssessment m_boxAssessment;
	private Set<CellSetAssessment> m_cellSetAssessments;
	
	private Set<Symbol> m_couldBeSymbolsSet;
	private Set<Symbol> m_ruledOutSymbolsSet;
	private int m_stepNumberOfLatestChange;

	CellAssessment(Cell cell, RowAssessment rowAssessment, ColumnAssessment columnAssessment, BoxAssessment boxAssessment, SymbolsToUse symbols) {
		m_cell = cell;
		m_rowAssessment = rowAssessment;
		m_columnAssessment = columnAssessment;
		m_boxAssessment = boxAssessment;
		m_cellSetAssessments = new LinkedHashSet<>();
		m_cellSetAssessments.add(rowAssessment);
		m_cellSetAssessments.add(columnAssessment);
		m_cellSetAssessments.add(boxAssessment);

		m_couldBeSymbolsSet = new LinkedHashSet<>(symbols.getSymbolSet());		
		m_ruledOutSymbolsSet = new LinkedHashSet<>();
		m_stepNumberOfLatestChange = -1;
	}

	Cell cell() 							{ return m_cell; }
	RowAssessment rowAssessment() 			{ return m_rowAssessment; }
	ColumnAssessment columnAssessment() 	{ return m_columnAssessment; }
	BoxAssessment boxAssessment() 			{ return m_boxAssessment; }
	
	boolean couldBe(Symbol symbol) {
		return m_couldBeSymbolsSet.contains(symbol);
	}

	int couldBeCount() {
		return m_couldBeSymbolsSet.size();
	}

	Set<Symbol> couldBeSymbols() {
		return new LinkedHashSet<>(m_couldBeSymbolsSet);
	}

	Symbol getOnlyCouldBeSymbolForCell() {
		Symbol symbol = null;
		if(couldBeCount() == 1) {
			symbol = m_couldBeSymbolsSet.stream().findFirst().get();
		}
		return symbol;
	}
	
	private void setStepNumber(int n) {
		m_stepNumberOfLatestChange = n;
//System.err.println("Setting cell " + cell().getOneBasedCellNumber() + " for step " + m_stepNumberOfLatestChange);			
	}
	
	public int stepNumberOfLatestChange() { 
		return m_stepNumberOfLatestChange;
	}
	
	Set<CellSetAssessment> cellSetAssessments() {
		return new LinkedHashSet<>(m_cellSetAssessments);
	}
	
	boolean isRuledOut(Symbol symbol) {
		return m_ruledOutSymbolsSet.contains(symbol);
	}
	
	int ruleOutSymbol(Symbol symbol, int stepNumber) {
		int changeCount = 0;
		if(!isRuledOut(symbol)) {
			m_ruledOutSymbolsSet.add(symbol);
			m_couldBeSymbolsSet.remove(symbol);
//			System.err.println("Ruling out symbol " + symbol + " for cell " + cell().getOneBasedCellNumber() + " for step " + stepNumber);			
			setStepNumber(stepNumber);
			changeCount++;
		}		
		return changeCount;
	}
	
	boolean ruleOutAllSymbolsExcept(List<Symbol> lPossibleSymbols, int stepNumber) {
		
		List<Symbol> lNoLongerPossibleSymbols = m_couldBeSymbolsSet.stream()
				.filter(couldBeSymbol -> !lPossibleSymbols.contains(couldBeSymbol))
				.collect(Collectors.toList());
		
		int changeCount = 0;		
		for(Symbol unwantedSymbol : lNoLongerPossibleSymbols) {
			changeCount += ruleOutSymbol(unwantedSymbol, stepNumber);
		}
		
		return changeCount > 0;
	}

	boolean ruleOutAllSymbolsExcept(Symbol symbol, int stepNumber) {
		List<Symbol> l = new ArrayList<>();
		l.add(symbol);
		return ruleOutAllSymbolsExcept(l, stepNumber);
	}

	boolean isAssigned() {
		return m_cell.isAssigned();
	}
	
	void assignmentMade(Symbol symbol, int stepNumber) {
		ruleOutAllSymbolsExcept(symbol, stepNumber);
	}
	
	@Override
	public int compareTo(CellAssessment c) {
		return m_cell.compareTo(c.m_cell);
	}

	String toCouldBeSymbolsString() {
		return Symbol.symbolCollectionToString(m_couldBeSymbolsSet);
	}	
}