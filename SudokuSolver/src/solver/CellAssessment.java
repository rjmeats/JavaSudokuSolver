package solver;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import diagnostics.FormatUtils;
import grid.Cell;
import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Puzzle;
import puzzle.Symbol;

public class CellAssessment implements Comparable<CellAssessment> {

	public Cell m_cell;
	RowAssessment m_row;
	ColumnAssessment m_column;
	BoxAssessment m_box;
	
	private LinkedHashSet<Symbol> m_couldBeSymbolsSet;
	private LinkedHashSet<Symbol> m_ruledOutSymbolsSet;

	CellAssessment(Cell cell, RowAssessment row, ColumnAssessment column, BoxAssessment box, List<Symbol> lAllSymbols) {
		m_cell = cell;
		m_row = row;
		m_column = column;
		m_box = box;
		m_couldBeSymbolsSet = new LinkedHashSet<>();
		m_ruledOutSymbolsSet = new LinkedHashSet<>();
		
		for(Symbol symbol : lAllSymbols) {
			m_couldBeSymbolsSet.add(symbol);
		}
	}

	RowAssessment getRow() { return m_row; }
	ColumnAssessment getColumn() { return m_column; }
	BoxAssessment getBox() { return m_box; }	
	
	List<Symbol> getCouldBeSymbolsList() {
		return new ArrayList<>(m_couldBeSymbolsSet);
	}
	
	Assignment checkForAssignableSymbol(int stepNumber) {
		Assignment a = null;
		if(!m_cell.isAssigned() && m_couldBeSymbolsSet.size() == 1) {
			for(Symbol symbol: m_couldBeSymbolsSet) {
				a = new Assignment(m_cell, symbol, AssignmentMethod.AutomatedDeduction, "Only symbol for cell", stepNumber);
			}
		}
		
		return a;
	}

	boolean couldBe(Symbol symbol) {
		return m_couldBeSymbolsSet.contains(symbol);
	}

	boolean isRuledOut(Symbol symbol) {
		return m_ruledOutSymbolsSet.contains(symbol);
	}
	
	boolean ruleOut(Symbol symbol) {
		boolean changed = false;
		if(!isRuledOut(symbol)) {
			if(couldBe(symbol)) {
				m_ruledOutSymbolsSet.add(symbol);
				m_couldBeSymbolsSet.remove(symbol);
				changed = true;
				Puzzle.L.info(".. ruled out cell " + m_cell.getCellNumber() + " : " + symbol.toString());				
			}
			else {
				// ???? Don't expect to hit this, means our maps have got out of alignment with each other.
			}
		}
		
		Puzzle.L.info(".. for cell " + m_cell.getCellNumber() + " symbol-could-be list = " + Symbol.symbolSetToString(m_couldBeSymbolsSet) + ":  ruled-out list = " + Symbol.symbolSetToString(m_ruledOutSymbolsSet));
		
		return changed;
	}
	
	boolean ruleOutAllBut(List<Symbol> lSymbols) {
		boolean changed = false;
		
		List<Symbol> lUnwantedSymbols = new ArrayList<>();
		for(Symbol couldBeSymbol : m_couldBeSymbolsSet) {
			if(!lSymbols.contains(couldBeSymbol)) {
				lUnwantedSymbols.add(couldBeSymbol);
			}
		}
		
		for(Symbol unwantedSymbol : lUnwantedSymbols) {
			boolean causedChange = ruleOut(unwantedSymbol);
			if(causedChange) {
				changed = true;
			}
		}
		
		return changed;
	}

	int couldBeCount() {
		return m_couldBeSymbolsSet.size();
	}
	
	String toCouldBeValuesString() {
		return Symbol.symbolSetToString(m_couldBeSymbolsSet);
	}	
	
	CellAssignmentStatus checkCellCanBeAssigned(Assignment assignment) {
		Symbol symbol = assignment.getSymbol();
		CellAssignmentStatus status = CellAssignmentStatus.CanBeAssigned;
		
		if(m_cell.isAssigned()) {
			status = CellAssignmentStatus.CellAlreadyAssigned;
		}
		else if(!couldBe(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOut;			
		}
		else if(isRuledOut(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOut;			
		}
		else if(getRow().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInRow;
		}
		else if(getColumn().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInColumn;			
		}
		else if(getBox().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInBox;			
		}

		return status;
	}
	
	CellAssignmentStatus setAsAssigned(Assignment assignment) {
		Puzzle.L.info("Trying assignment " + assignment.toString());
		CellAssignmentStatus status = checkCellCanBeAssigned(assignment);
		if(status == CellAssignmentStatus.CanBeAssigned) {
			Puzzle.L.info(".. assignment is possible ...");
			
			m_cell.assign(assignment);

			// Tidy up map of which symbols this cell could/could-not be
			for(Symbol couldBeSymbol : m_couldBeSymbolsSet) {
				if(assignment.getSymbol() != couldBeSymbol) {
					m_ruledOutSymbolsSet.add(couldBeSymbol);
				}
			}
			m_couldBeSymbolsSet.clear();
			m_couldBeSymbolsSet.add(assignment.getSymbol());

			getRow().markAsAssigned(assignment, this);
			getColumn().markAsAssigned(assignment, this);
			getBox().markAsAssigned(assignment, this);
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cell.getCellNumber() + " complete");
		}
		else {
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cell.getCellNumber() + " not possible: " + status.name());			
		}
		
		return status;		
	}
	
	void setAsAlreadyAssigned(Assignment assignment) {
		// Tidy up map of which symbols this cell could/could-not be
		for(Symbol couldBeSymbol : m_couldBeSymbolsSet) {
			if(assignment.getSymbol() != couldBeSymbol) {
				m_ruledOutSymbolsSet.add(couldBeSymbol);
			}
		}
		m_couldBeSymbolsSet.clear();
		m_couldBeSymbolsSet.add(assignment.getSymbol());

		getRow().markAsAssigned(assignment, this);
		getColumn().markAsAssigned(assignment, this);
		getBox().markAsAssigned(assignment, this);
	}
	
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
			String representation = "" + ca.toCouldBeValuesString();
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

	@Override
	public int compareTo(CellAssessment c) {
		return m_cell.getCellNumber() - c.m_cell.getCellNumber();
	}

}