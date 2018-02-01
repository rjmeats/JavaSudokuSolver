import java.util.List;
import java.util.Comparator;
import java.util.HashMap;

public class Cell {

	private int m_cellNumber;
	private Row m_row;
	private Column m_column;
	private Box m_box;

	private Assignment m_assignment;
	private HashMap<CellSymbol, CellSymbol> m_mapCouldBeSymbols;
	private HashMap<CellSymbol, CellSymbol> m_mapRuledOutSymbols;

	Cell(int cellNumber, Row row, Column column, Box box, List<CellSymbol> lAllSymbols) {
		m_cellNumber = cellNumber;
		m_row = row;
		m_column = column;
		m_box = box;
		
		m_assignment = null;		
		m_mapCouldBeSymbols = new HashMap<>();
		m_mapRuledOutSymbols = new HashMap<>();
		
		for(CellSymbol symbol : lAllSymbols)
		{
			m_mapCouldBeSymbols.put(symbol, symbol);
		}
	}
	
	public Row getRow() { return m_row; }
	public Column getColumn() { return m_column; }
	public Box getBox() { return m_box; }
	
	public String identifyLocation()
	{
		return "[" + m_column.getColumnNumber() + "," + m_row.getRowNumber() + "]";
	}
	
	CellAssignmentStatus setAsAssigned(Assignment assignment)
	{
		Puzzle.L.info("Trying assignment " + assignment.toString());
		CellAssignmentStatus status = checkCellCanBeAssigned(assignment);
		if(status == CellAssignmentStatus.CanBeAssigned)
		{
			Puzzle.L.info(".. assignment is possible ...");
			
			m_assignment = assignment;

			// Tidy up map of which symbols this cell could/could-not be
			for(CellSymbol couldBeSymbol : m_mapCouldBeSymbols.keySet())
			{
				if(assignment.getSymbol() != couldBeSymbol)
				{
					m_mapRuledOutSymbols.put(couldBeSymbol, couldBeSymbol);
				}
			}
			m_mapCouldBeSymbols.clear();
			m_mapCouldBeSymbols.put(assignment.getSymbol(), assignment.getSymbol());

			m_row.markAsAssigned(assignment);
			m_column.markAsAssigned(assignment);
			m_box.markAsAssigned(assignment);
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().getRepresentation() + " to cell " + m_cellNumber + " complete");
		}
		else
		{
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().getRepresentation() + " to cell " + m_cellNumber + " not possible: " + status.name());			
		}
		
		return status;		
	}
	
	CellAssignmentStatus checkCellCanBeAssigned(Assignment assignment)
	{
		CellSymbol symbol = assignment.getSymbol();
		
		CellAssignmentStatus status = CellAssignmentStatus.CanBeAssigned;
		
		if(isAssigned())
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
		else if(m_row.symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInRow;
		}
		else if(m_column.symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInColumn;			
		}
		else if(m_box.symbolAlreadyAssigned(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyAssignedInBox;			
		}

		return status;
	}
	
	boolean isAssigned()
	{
		return m_assignment != null;
	}
	
	Assignment getAssignment()
	{
		return m_assignment;
	}
	
	boolean couldBe(CellSymbol symbol)
	{
		return m_mapCouldBeSymbols.containsKey(symbol);
	}

	boolean isRuledOut(CellSymbol symbol)
	{
		return m_mapRuledOutSymbols.containsKey(symbol);
	}
	
	boolean ruleOut(CellSymbol symbol)
	{
		boolean changed = false;
		if(!isRuledOut(symbol))
		{
			if(couldBe(symbol))
			{
				m_mapRuledOutSymbols.put(symbol,symbol);
				m_mapCouldBeSymbols.remove(symbol);
				changed = true;
				Puzzle.L.info(".. ruled out cell " + getCellNumber() + " : " + symbol.getRepresentation());				
			}
			else
			{
				// ???? Don't expect to hit this, means our maps have got out of alignment with each other.
			}
		}
		
		Puzzle.L.info(".. for cell " + getCellNumber() + " symbol-could-be list = " + CellSymbol.symbolMapToString(m_mapCouldBeSymbols) + ":  ruled-out list = " + CellSymbol.symbolMapToString(m_mapRuledOutSymbols));
		
		return changed;
	}
	
	int couldBeCount()
	{
		return m_mapCouldBeSymbols.size();
	}
	
	String toCouldBeValuesString()
	{
		return CellSymbol.symbolMapToString(m_mapCouldBeSymbols);
	}	
	
	int getCellNumber()
	{
		return m_cellNumber;
	}
	
	static class CellNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Cell numbering"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			return(FormatUtils.padRight(c.m_cellNumber, 5));
		}
	}

	static class BoxNumberDisplayer implements CellContentDisplayer {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			return(FormatUtils.padRight(c.m_box.getBoxNumber(), 5));
		}
	}
	
	static class CouldBeValueCountDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Cell 'Could-be-value' count"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			String representation = "" + c.couldBeCount();
			if(!c.isAssigned() && c.couldBeCount() == 1)
			{
				representation += "!";
			}
			else if (c.isAssigned() && c.getAssignment().getMethod() == AssignmentMethod.Given)
			{
				representation = "[" + representation + "]";				
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}
	
	static class CouldBeValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Cell 'Could-be' values"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			return(FormatUtils.padRight(c.toCouldBeValuesString(), 21));
		}
	}
	
	static class AssignedValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			String representation = "-";
			if(c.isAssigned())
			{
				CellSymbol symbol = c.getAssignment().getSymbol();
				representation = symbol.getRepresentation();
				if(highlight)
				{
					representation += "*";
				}
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}

	static class SortByCellNumber implements Comparator<Cell>
	{
	    // Used for sorting in ascending order of
	    // roll number
	    public int compare(Cell c1, Cell c2)
	    {
	        return c1.m_cellNumber - c2.m_cellNumber;
	    }
	}
}

enum CellAssignmentStatus
{
	CanBeAssigned, CellAlreadyAssigned, SymbolAlreadyRuledOut, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}

