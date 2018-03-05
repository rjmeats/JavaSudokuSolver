package solver;

import java.util.Set;

import java.util.stream.Collectors;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

import grid.Cell;

import puzzle.Symbol;
import puzzle.SymbolsToUse;

import diagnostics.FormatUtils;

public class CellAssessment implements Comparable<CellAssessment> {

	private Cell m_cell;
	
	private RowAssessment m_rowAssessment;
	private ColumnAssessment m_columnAssessment;
	private BoxAssessment m_boxAssessment;
	private Set<CellSetAssessment> m_cellSetAssessments;
	
	private Set<Symbol> m_couldBeSymbolsSet;
	private Set<Symbol> m_ruledOutSymbolsSet;

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

	Set<Symbol> getCouldBeSymbols() {
		return new LinkedHashSet<>(m_couldBeSymbolsSet);
	}

	Set<CellSetAssessment> getCellSetAssessments() {
		return new LinkedHashSet<>(m_cellSetAssessments);
	}
	
	boolean isRuledOut(Symbol symbol) {
		return m_ruledOutSymbolsSet.contains(symbol);
	}
	
	int ruleOutSymbol(Symbol symbol) {
		int changeCount = 0;
		if(!isRuledOut(symbol)) {
			m_ruledOutSymbolsSet.add(symbol);
			m_couldBeSymbolsSet.remove(symbol);
			changeCount++;
		}		
		return changeCount;
	}
	
	boolean ruleOutAllSymbolsExcept(List<Symbol> lPossibleSymbols) {
		
		List<Symbol> lNoLongerPossibleSymbols = m_couldBeSymbolsSet.stream()
				.filter(couldBeSymbol -> !lPossibleSymbols.contains(couldBeSymbol))
				.collect(Collectors.toList());
		
		int changeCount = 0;		
		for(Symbol unwantedSymbol : lNoLongerPossibleSymbols) {
			changeCount += ruleOutSymbol(unwantedSymbol);
		}
		
		return changeCount > 0;
	}

	boolean isAssigned() {
		return m_cell.isAssigned();
	}
	
	void assignmentMade(Symbol symbol) {		
		List<Symbol> l = new ArrayList<>();
		l.add(symbol);
		ruleOutAllSymbolsExcept(l);
	}
	
	@Override
	public int compareTo(CellAssessment c) {
		return m_cell.getCellNumber() - c.m_cell.getCellNumber();
	}

	String toCouldBeSymbolsString() {
		return Symbol.symbolSetToString(m_couldBeSymbolsSet);
	}	
	
	// ==============================================================
/*	
	public static class CouldBeValueCountDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(CellAssessment ca, boolean highlight) {
			Cell c = ca.m_cell;
			String representation = "" + ca.couldBeCount();
			if(!c.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (c.isAssigned() && c.getAssignment().getMethod() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(c.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}
	
	public static class CouldBeValueDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be' values"; }
		
		public String getContent(CellAssessment ca, boolean highlight) {
			Cell c = ca.m_cell;
			String representation = "" + ca.toCouldBeSymbolsString();
			if(!c.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (c.isAssigned() && c.getAssignment().getMethod() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(c.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 17));
		}
	}
*/
	
	public static class CellNumberDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Cell numbering"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.getCellNumber(), 5));
		}
	}

	public static class BoxNumberDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.box().getBoxNumber(), 5));
		}
	}
	
	public static class AssignedValueDisplay implements CellContentProvider {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(Cell cell) {
			String representation = ".";
			if(cell.isAssigned())
			{
				Symbol symbol = cell.getAssignment().getSymbol();
				representation = symbol.getRepresentation();
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}

}