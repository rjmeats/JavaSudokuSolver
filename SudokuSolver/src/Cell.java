import java.util.List;
import java.util.Comparator;

public class Cell {

	int m_cellNumber;
	Row m_row;
	Column m_column;
	Box m_box;
	CellValueAssessment m_value;
	
	Cell(int cellNumber, Row row, Column column, Box box, List<CellSymbol> lSymbols) {
		m_cellNumber = cellNumber;
		m_row = row;
		m_column = column;
		m_box = box;
		m_value = new CellValueAssessment(this, lSymbols);
	}
	
	CellAssignmentStatus setAsGiven(CellSymbol symbol)
	{
		return setAsAssigned(AssignmentMethod.Given, symbol, 0);
	}

	CellAssignmentStatus setAsAssigned(AssignmentMethod method, CellSymbol symbol, int assignmentStep)
	{
		Puzzle.L.info("Assigning symbol " + symbol.getRepresentation() + " to cell " + m_cellNumber);
		CellAssignmentStatus status = checkCellCanBeSet(symbol);
		if(status == CellAssignmentStatus.CanBeAssigned)
		{
			Puzzle.L.info(".. assignment is possible ...");
			m_value.setAsAssigned(method, symbol, assignmentStep);
			m_row.markAsAssigned(symbol, this);
			m_column.markAsAssigned(symbol, this);
			m_box.markAsAssigned(symbol, this);
			Puzzle.L.info(".. assignment of symbol " + symbol.getRepresentation() + " to cell " + m_cellNumber + " complete");
		}
		else
		{
			Puzzle.L.info(".. assignment of symbol " + symbol.getRepresentation() + " to cell " + m_cellNumber + " not possible: " + status.name());			
		}
		
		return status;		
	}
	
	void ruleOut(CellSymbol symbol)
	{
		m_value.ruleOut(symbol);
	}
	
	CellAssignmentStatus checkCellCanBeSet(CellSymbol symbol)
	{
		CellAssignmentStatus status = CellAssignmentStatus.CanBeAssigned;
		
		if(m_value.isAssigned())
		{
			status = CellAssignmentStatus.CellAlreadyAssigned;
		}
		else if(!m_value.couldBe(symbol))
		{
			status = CellAssignmentStatus.SymbolAlreadyRuledOut;			
		}
		else if(m_value.isRuledOut(symbol))
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
		return m_value.isAssigned();
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
			String representation = "" + c.m_value.couldBeCount();
			if(!c.isAssigned() && c.m_value.couldBeCount() == 1)
			{
				representation += "!";
			}
			else if (c.isAssigned() && c.m_value.m_method == AssignmentMethod.Given)
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
			return(FormatUtils.padRight(c.m_value.couldBeValuesString(), 21));
		}
	}
	
	static class AssignedValueDisplay implements CellContentDisplayer {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(Cell c, boolean highlight)
		{
			CellSymbol symbol = c.m_value.m_assignment;
			String representation = symbol == null ? "-" : symbol.getRepresentation();
			if(highlight)
			{
				representation += "*";
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

