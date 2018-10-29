package grid;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Represents a cell in a Sudoku grid. 
 */

public class Cell implements Comparable<Cell> {

	// NB Internally (when used for grid calculations), cell numbers start at 0. 
	// But for ease of human comprehension, show them as starting from 1 in grid-based output  
	private int m_cellNumber;
	
	// Each cell belongs to a column, row, and a box in the grid.
	private Column m_column;
	private Row m_row;
	private Box m_box;
	
	// Has an assignment of a symbol been made to the cell ? Null if not. 
	private Assignment m_assignment;

	Cell(int cellNumber, Column column, Row row, Box box) {
		m_cellNumber = cellNumber;
		m_column = column;
		m_row = row;
		m_box = box;		
		m_assignment = null;		
	}
	
	public int cellNumber() 			{ return m_cellNumber; }
	private int cellNumberForDisplay() 	{ return m_cellNumber+1; }
	public Column column() 				{ return m_column; }
	public Row row() 					{ return m_row; }
	public Box box() 					{ return m_box; }
	
	/**
	 * @return NB Returns null if no symbol assignment has yet been made for this cell.
	 */
	public Assignment assignment() 		{ return m_assignment; }
	
	/**
	 * Make an assignment of a symbol to this cell.
	 * 
	 * @param assignment Details of the assignment being made.
	 */
	public void assign(Assignment assignment) { 
		m_assignment = assignment;
	}
	
	public boolean isAssigned() {
		return m_assignment != null;
	}
	
	/**
	 * @return NB Returns null if no symbol assigned to the cell.
	 */
	public Symbol assignedSymbol() {
		return (m_assignment != null) ? m_assignment.symbol() : null;
	}
		
	public int compareTo(Cell c) {
		return m_cellNumber - c.m_cellNumber;
	}

	// Use 1-based numbering for showing a cell number in external displays of grid contents.
	public String getRepresentation() {
		return "Cell " + cellNumberForDisplay();
	}

	public String numberOnlyRepresentation() {
		return "" + cellNumberForDisplay();
	}

	/**
	 * @return A string [col,row] showing where the cell is in within its grid.
	 */
	public String gridLocation() {
		return "[" + (m_column.numberOnlyRepresentation()) + "," + (m_row.numberOnlyRepresentation()) + "]";
	}

	// For debuggers only
	public String toString() {
		return "Cell no=" + cellNumber();
	}

	/**
	 * Generates a string recording each cell in a collection.
	 * 
	 * @param cells A collection (e.g. a List or a Set) of cells
	 * 
	 * @return Space-separated, ordered list of the cell numbers as a string 
	 */
	public static String cellCollectionRepresentation(Collection<Cell> cells) {		
		// Functional approach.
		return cells.stream()
				.sorted()
				.map(c -> c.numberOnlyRepresentation())
				.collect(Collectors.joining(" "));
	}
}
