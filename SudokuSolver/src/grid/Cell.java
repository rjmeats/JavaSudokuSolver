package grid;
import java.util.List;

import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.CellContentDisplayer;
import puzzle.FormatUtils;
import puzzle.Puzzle;
import puzzle.Symbol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Cell implements Comparable<Cell> {

	// Where the cell is in its grid
	private int m_cellNumber;
	private Row m_row;
	private Column m_column;
	private Box m_box;

	// Items relating to assigning a symbol to the cell by the solver program.
	private Assignment m_assignment;

	Cell(int cellNumber, Row row, Column column, Box box) {
		m_cellNumber = cellNumber;
		m_row = row;
		m_column = column;
		m_box = box;
		
		m_assignment = null;		
	}
	
	public Row getRow() { return m_row; }
	public Column getColumn() { return m_column; }
	public Box getBox() { return m_box; }
	
	public String getColumnAndRowLocationString()
	{
		return "[" + m_column.getColumnNumber() + "," + m_row.getRowNumber() + "]";
	}
	
	public String toString()
	{
		return "Cell no=" + getCellNumber();
	}

	void assign(Assignment assignment) {
		m_assignment = assignment;
	}
	
	boolean isAssigned()
	{
		return m_assignment != null;
	}
	
	Assignment getAssignment()
	{
		return m_assignment;
	}
	
	int getCellNumber()
	{
		return m_cellNumber;
	}
	
	// Different sort options, are they needed ???? How do they differ ?
	static class SortByCellNumber implements Comparator<Cell>
	{
	    // Used for sorting in ascending order of
	    // roll number
	    public int compare(Cell c1, Cell c2)
	    {
	        return c1.m_cellNumber - c2.m_cellNumber;
	    }
	}

	@Override
	public int compareTo(Cell c) {
		return m_cellNumber - c.m_cellNumber;
	}
}

enum CellAssignmentStatus
{
	CanBeAssigned, CellAlreadyAssigned, SymbolAlreadyRuledOut, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}




class CellAssessment implements Comparable<CellAssessment> {

	Cell m_cell;
	RowAssessment m_row;
	ColumnAssessment m_column;
	BoxAssessment m_box;
	
	private HashMap<Symbol, Symbol> m_mapCouldBeSymbols;
	private HashMap<Symbol, Symbol> m_mapRuledOutSymbols;

	CellAssessment(Cell cell, RowAssessment row, ColumnAssessment column, BoxAssessment box, List<Symbol> lAllSymbols) {
		m_cell = cell;
		m_row = row;
		m_column = column;
		m_box = box;
		m_mapCouldBeSymbols = new HashMap<>();
		m_mapRuledOutSymbols = new HashMap<>();
		
		for(Symbol symbol : lAllSymbols)
		{
			m_mapCouldBeSymbols.put(symbol, symbol);
		}
	}

	RowAssessment getRow() { return m_row; }
	ColumnAssessment getColumn() { return m_column; }
	BoxAssessment getBox() { return m_box; }	
	
	List<Symbol> getCouldBeSymbolsList()
	{
		return new ArrayList<>(m_mapCouldBeSymbols.keySet());
	}
	
	Assignment checkForAssignableSymbol(int stepNumber)
	{
		Assignment a = null;
		if(!m_cell.isAssigned() && m_mapCouldBeSymbols.size() == 1)
		{
			for(Symbol symbol: m_mapCouldBeSymbols.keySet())
			{
				a = new Assignment(m_cell, symbol, AssignmentMethod.AssignedSymbolToCell, stepNumber);
			}
		}
		
		return a;
	}

	boolean couldBe(Symbol symbol)
	{
		return m_mapCouldBeSymbols.containsKey(symbol);
	}

	boolean isRuledOut(Symbol symbol)
	{
		return m_mapRuledOutSymbols.containsKey(symbol);
	}
	
	boolean ruleOut(Symbol symbol)
	{
		boolean changed = false;
		if(!isRuledOut(symbol))
		{
			if(couldBe(symbol))
			{
				m_mapRuledOutSymbols.put(symbol,symbol);
				m_mapCouldBeSymbols.remove(symbol);
				changed = true;
				Puzzle.L.info(".. ruled out cell " + m_cell.getCellNumber() + " : " + symbol.toString());				
			}
			else
			{
				// ???? Don't expect to hit this, means our maps have got out of alignment with each other.
			}
		}
		
		Puzzle.L.info(".. for cell " + m_cell.getCellNumber() + " symbol-could-be list = " + Symbol.symbolMapToString(m_mapCouldBeSymbols) + ":  ruled-out list = " + Symbol.symbolMapToString(m_mapRuledOutSymbols));
		
		return changed;
	}
	
	boolean ruleOutAllBut(List<Symbol> lSymbols)
	{
		boolean changed = false;
		
		List<Symbol> lUnwantedSymbols = new ArrayList<>();
		for(Symbol couldBeSymbol : m_mapCouldBeSymbols.values())
		{
			if(!lSymbols.contains(couldBeSymbol))
			{
				lUnwantedSymbols.add(couldBeSymbol);
			}
		}
		
		for(Symbol unwantedSymbol : lUnwantedSymbols)
		{
			boolean causedChange = ruleOut(unwantedSymbol);
			if(causedChange)
			{
				changed = true;
			}
		}
		
		return changed;
	}

	int couldBeCount()
	{
		return m_mapCouldBeSymbols.size();
	}
	
	String toCouldBeValuesString()
	{
		return Symbol.symbolMapToString(m_mapCouldBeSymbols);
	}	
	
	CellAssignmentStatus checkCellCanBeAssigned(Assignment assignment)
	{
		Symbol symbol = assignment.getSymbol();
		
		CellAssignmentStatus status = CellAssignmentStatus.CanBeAssigned;
		
		if(m_cell.isAssigned())
		{
			status = CellAssignmentStatus.CellAlreadyAssigned;
		}
		else if(!couldBe(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyRuledOut;			
		}
		else if(isRuledOut(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyRuledOut;			
		}
		else if(getRow().symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInRow;
		}
		else if(getColumn().symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInColumn;			
		}
		else if(getBox().symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInBox;			
		}

		return status;
	}
	
	CellAssignmentStatus setAsAssigned(Assignment assignment)
	{
		Puzzle.L.info("Trying assignment " + assignment.toString());
		CellAssignmentStatus status = checkCellCanBeAssigned(assignment);
		if(status == CellAssignmentStatus.CanBeAssigned)
		{
			Puzzle.L.info(".. assignment is possible ...");
			
			m_cell.assign(assignment);

			// Tidy up map of which symbols this cell could/could-not be
			for(Symbol couldBeSymbol : m_mapCouldBeSymbols.keySet())
			{
				if(assignment.getSymbol() != couldBeSymbol)
				{
					m_mapRuledOutSymbols.put(couldBeSymbol, couldBeSymbol);
				}
			}
			m_mapCouldBeSymbols.clear();
			m_mapCouldBeSymbols.put(assignment.getSymbol(), assignment.getSymbol());

			getRow().markAsAssigned(assignment, this);
			getColumn().markAsAssigned(assignment, this);
			getBox().markAsAssigned(assignment, this);
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cell.getCellNumber() + " complete");
		}
		else
		{
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cell.getCellNumber() + " not possible: " + status.name());			
		}
		
		return status;		
	}
	
	static class CouldBeValueCountDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(CellAssessment ca, boolean highlight)
		{
			Cell c = ca.m_cell;
			String representation = "" + ca.couldBeCount();
			if(!c.isAssigned() && ca.couldBeCount() == 1)
			{
				representation = "*"+ representation;
			}
			else if (c.isAssigned() && c.getAssignment().getMethod() == AssignmentMethod.Given)
			{
				representation = "~" + representation;				
			}
			else if(c.isAssigned())
			{
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}
	
	static class CouldBeValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Cell 'Could-be' values"; }
		
		public String getContent(CellAssessment ca, boolean highlight)
		{
			Cell c = ca.m_cell;
			String representation = "" + ca.toCouldBeValuesString();
			if(!c.isAssigned() && ca.couldBeCount() == 1)
			{
				representation = "*"+ representation;
			}
			else if (c.isAssigned() && c.getAssignment().getMethod() == AssignmentMethod.Given)
			{
				representation = "~" + representation;				
			}
			else if(c.isAssigned())
			{
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 17));
		}
	}
	
	static class CellNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Cell numbering"; }
		
		public String getContent(CellAssessment ca, boolean highlight)
		{
			Cell c = ca.m_cell;
			return(FormatUtils.padRight(c.getCellNumber(), 5));
		}
	}

	static class BoxNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(CellAssessment ca, boolean highlight)
		{
			Cell c = ca.m_cell;
			return(FormatUtils.padRight(c.getBox().getBoxNumber(), 5));
		}
	}
	
	static class AssignedValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(CellAssessment ca, boolean highlight)
		{
			Cell c = ca.m_cell;
			String representation = "-";
			if(c.isAssigned())
			{
				Symbol symbol = c.getAssignment().getSymbol();
				representation = symbol.getGridRepresentation();
				if(highlight)
				{
					representation += "*";
				}
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}

	static class SortByCellNumber implements Comparator<CellAssessment>
	{
	    // Used for sorting in ascending order of
	    // roll number
	    public int compare(CellAssessment c1, CellAssessment c2)
	    {
	        return c1.m_cell.getCellNumber() - c2.m_cell.getCellNumber();
	    }
	}

	@Override
	public int compareTo(CellAssessment c) {
		return m_cell.getCellNumber() - c.m_cell.getCellNumber();
	}

}