package grid;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import puzzle.Assignment;
import puzzle.Symbol;

public class Cell implements Comparable<Cell> {

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
	
	public Row row() 					{ return m_row; }
	public Column column() 				{ return m_column; }
	public Box box() 					{ return m_box; }
	public Assignment assignment() 		{ return m_assignment; }
	public int cellNumber() 			{ return m_cellNumber; }
	
	// Use one-based numbering for external descriptions
	public String getGridLocationString() {
		return "[" + (m_column.getNumberOnlyRepresentation()) + "," + (m_row.getNumberOnlyRepresentation()) + "]";
	}

	public String getRepresentation() {
		return "Cell " + (cellNumber()+1);
	}

	// For debuggers only
	public String toString() {
		return "Cell no=" + cellNumber();
	}

	public void assign(Assignment assignment) { 
		m_assignment = assignment; 
	}
	
	public boolean isAssigned() {
		return m_assignment != null;
	}
	
	public Symbol getAssignedSymbol() {
		return (m_assignment != null) ? m_assignment.symbol() : null;
	}
		
	@Override
	public int compareTo(Cell c) {
		return m_cellNumber - c.m_cellNumber;
	}

	// For external consumption, so use one-based cell numbering
	public static String cellCollectionToString(Collection<Cell> cells) {
		List<Cell> l = new ArrayList<>(cells);
		Collections.sort(l);
		StringBuilder sb = new StringBuilder();
		for(Cell cell: l) {
			sb.append(cell.cellNumber()+1).append(" ");
		}
		return sb.toString().trim();
	}
}
