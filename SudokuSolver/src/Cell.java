import java.util.List;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;

public class Cell {

	// Where the cell is in its grid
	private int m_cellNumber;
	private Row m_row;
	private Column m_column;
	private Box m_box;

	// Items relating to assigning a symbol to the cell
	private Assignment m_assignment;
	private HashMap<Symbol, Symbol> m_mapCouldBeSymbols;
	private HashMap<Symbol, Symbol> m_mapRuledOutSymbols;

	Cell(int cellNumber, Row row, Column column, Box box, List<Symbol> lAllSymbols) {
		m_cellNumber = cellNumber;
		m_row = row;
		m_column = column;
		m_box = box;
		
		m_assignment = null;		
		m_mapCouldBeSymbols = new HashMap<>();
		m_mapRuledOutSymbols = new HashMap<>();
		
		for(Symbol symbol : lAllSymbols)
		{
			m_mapCouldBeSymbols.put(symbol, symbol);
		}
	}
	
	public Row getRow() { return m_row; }
	public Column getColumn() { return m_column; }
	public Box getBox() { return m_box; }
	
	List<Symbol> getCouldBeSymbolsList()
	{
		return new ArrayList<>(m_mapCouldBeSymbols.keySet());
	}
	
	public String getColumnAndRowLocationString()
	{
		return "[" + m_column.getColumnNumber() + "," + m_row.getRowNumber() + "]";
	}
	
	Assignment checkForAssignableSymbol(int stepNumber)
	{
		Assignment a = null;
		if(!isAssigned() && m_mapCouldBeSymbols.size() == 1)
		{
			for(Symbol symbol: m_mapCouldBeSymbols.keySet())
			{
				a = new Assignment(this, symbol, AssignmentMethod.AssignedSymbolToCell, stepNumber);
			}
		}
		
		return a;
	}

	public String toString()
	{
		return "Cell no=" + getCellNumber();
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
			for(Symbol couldBeSymbol : m_mapCouldBeSymbols.keySet())
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
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cellNumber + " complete");
		}
		else
		{
			Puzzle.L.info(".. assignment of symbol " + assignment.getSymbol().toString() + " to cell " + m_cellNumber + " not possible: " + status.name());			
		}
		
		return status;		
	}
	
	CellAssignmentStatus checkCellCanBeAssigned(Assignment assignment)
	{
		Symbol symbol = assignment.getSymbol();
		
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
				Puzzle.L.info(".. ruled out cell " + getCellNumber() + " : " + symbol.toString());				
			}
			else
			{
				// ???? Don't expect to hit this, means our maps have got out of alignment with each other.
			}
		}
		
		Puzzle.L.info(".. for cell " + getCellNumber() + " symbol-could-be list = " + Symbol.symbolMapToString(m_mapCouldBeSymbols) + ":  ruled-out list = " + Symbol.symbolMapToString(m_mapRuledOutSymbols));
		
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
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			String representation = "" + c.couldBeCount();
			if(!c.isAssigned() && c.couldBeCount() == 1)
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
		
		public String getContent(Cell c, boolean highlight)
		{
			String representation = "" + c.toCouldBeValuesString();
			if(!c.isAssigned() && c.couldBeCount() == 1)
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
	
	static class AssignedValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(Cell c, boolean highlight)
		{
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

