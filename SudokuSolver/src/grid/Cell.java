package grid;

import puzzle.Assignment;
import puzzle.Symbol;

public class Cell implements Comparable<Cell> {

	// Where the cell is in its grid
	private int m_cellNumber;
	private Row m_row;
	private Column m_column;
	private Box m_box;
	private Assignment m_assignment;

	Cell(int cellNumber, Row row, Column column, Box box) {
		m_cellNumber = cellNumber;
		m_row = row;
		m_column = column;
		m_box = box;		
		m_assignment = null;		
	}
	
	public Row getRow() 				{ return m_row; }
	public Column getColumn() 			{ return m_column; }
	public Box getBox() 				{ return m_box; }
	public Assignment getAssignment() 	{ return m_assignment; }
	public int getCellNumber() 			{ return m_cellNumber; }
	
	public String getLocationString() {
		return "[" + m_column.getColumnNumber() + "," + m_row.getRowNumber() + "]";
	}
	
	public String toString() {
		return "Cell no=" + getCellNumber();
	}

	public void assign(Assignment assignment) { 
		m_assignment = assignment; 
	}
	
	public boolean isAssigned() {
		return m_assignment != null;
	}
	
	public Symbol getAssignedSymbol() {
		return (m_assignment != null) ? m_assignment.getSymbol() : null;
	}
		
	@Override
	public int compareTo(Cell c) {
		return m_cellNumber - c.m_cellNumber;
	}
}
