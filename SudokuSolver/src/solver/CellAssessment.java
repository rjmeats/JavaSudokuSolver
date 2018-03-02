package solver;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

import grid.Cell;

import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

import diagnostics.FormatUtils;

public class CellAssessment implements Comparable<CellAssessment> {

	Cell m_cell;
	
	private RowAssessment m_rowAssessment;
	private ColumnAssessment m_columnAssessment;
	private BoxAssessment m_boxAssessment;
	
	private Set<Symbol> m_couldBeSymbolsSet;
	private Set<Symbol> m_ruledOutSymbolsSet;

	CellAssessment(Cell cell, RowAssessment rowAssessment, ColumnAssessment columnAssessment, BoxAssessment boxAssessment, SymbolsToUse symbols) {
		m_cell = cell;
		m_rowAssessment = rowAssessment;
		m_columnAssessment = columnAssessment;
		m_boxAssessment = boxAssessment;

		m_couldBeSymbolsSet = new LinkedHashSet<>(symbols.getSymbolSet());		
		m_ruledOutSymbolsSet = new LinkedHashSet<>();
	}

	Cell getCell() 					{ return m_cell; }
	RowAssessment getRow() 			{ return m_rowAssessment; }
	ColumnAssessment getColumn() 	{ return m_columnAssessment; }
	BoxAssessment getBox() 			{ return m_boxAssessment; }	
	
	boolean couldBe(Symbol symbol) {
		return m_couldBeSymbolsSet.contains(symbol);
	}

	int couldBeCount() {
		return m_couldBeSymbolsSet.size();
	}
	
	boolean isRuledOut(Symbol symbol) {
		return m_ruledOutSymbolsSet.contains(symbol);
	}
	
	int ruleOut(Symbol symbol) {
		int changeCount = 0;
		if(!isRuledOut(symbol)) {
			m_ruledOutSymbolsSet.add(symbol);
			m_couldBeSymbolsSet.remove(symbol);
			changeCount++;
		}		
		return changeCount;
	}
	
	boolean ruleOutAllExcept(List<Symbol> lPossibleSymbols) {
		
		List<Symbol> lNoLongerPossibleSymbols = m_couldBeSymbolsSet.stream()
				.filter(couldBeSymbol -> !lPossibleSymbols.contains(couldBeSymbol))
				.collect(Collectors.toList());
		
		int changeCount = 0;		
		for(Symbol unwantedSymbol : lNoLongerPossibleSymbols) {
			changeCount += ruleOut(unwantedSymbol);
		}
		
		return changeCount > 0;
	}

	// If there is only one symbol which can still be assigned to this cell, then we have an assignment 
	Assignment hasAssignmentAvailable(int stepNumber) {
		Assignment a = null;
		if(!m_cell.isAssigned() && m_couldBeSymbolsSet.size() == 1) {
			Symbol symbol = m_couldBeSymbolsSet.stream().findFirst().get();
			a = new Assignment(m_cell, symbol, AssignmentMethod.AutomatedDeduction, "Only symbol still possible for cell", stepNumber);
		}		
		return a;
	}

	void assignmentMade(Symbol symbol) {		
		List<Symbol> l = new ArrayList<>();
		l.add(symbol);
		ruleOutAllExcept(l);
	}
	
	@Override
	public int compareTo(CellAssessment c) {
		return m_cell.getCellNumber() - c.m_cell.getCellNumber();
	}

	String toCouldBeSymbolsString() {
		return Symbol.symbolSetToString(m_couldBeSymbolsSet);
	}	
	
	// ==============================================================
	
	public static class CouldBeValueCountDisplay implements CellContentDisplayer {
		
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
	
	public static class CouldBeValueDisplay implements CellContentDisplayer {
		
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
	
	public static class CellNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Cell numbering"; }
		
		public String getContent(CellAssessment ca, boolean highlight) {
			Cell c = ca.m_cell;
			return(FormatUtils.padRight(c.getCellNumber(), 5));
		}
	}

	public static class BoxNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(CellAssessment ca, boolean highlight) {
			Cell c = ca.m_cell;
			return(FormatUtils.padRight(c.getBox().getBoxNumber(), 5));
		}
	}
	
	public static class AssignedValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(CellAssessment ca, boolean highlight) {
			Cell c = ca.m_cell;
			String representation = "-";
			if(c.isAssigned())
			{
				Symbol symbol = c.getAssignment().getSymbol();
				representation = symbol.getRepresentation();
				if(highlight) {
					representation += "*";
				}
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}

}