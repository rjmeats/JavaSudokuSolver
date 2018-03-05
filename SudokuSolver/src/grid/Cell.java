package grid;

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
	public Assignment getAssignment() 	{ return m_assignment; }
	public int getCellNumber() 			{ return m_cellNumber; }
	
	public int getOneBasedCellNumber() 	{ return m_cellNumber+1; }

	public String getGridLocationString() {
		return "{" + m_column.getColumnNumber() + "," + m_row.getRowNumber() + "}";
	}
	
	public String getOneBasedGridLocationString() {
		return "[" + (m_column.getColumnNumber()+1) + "," + (m_row.getRowNumber()+1) + "]";
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

	public static String cellListToString(List<Cell> lIn) {
		List<Cell> l = new ArrayList<>(lIn);
		StringBuilder sb = new StringBuilder();
		Collections.sort(l);
		for(Cell cell: l) {
			sb.append(cell.getOneBasedCellNumber()).append(" ");
		}
		return sb.toString().trim();
	}

	static boolean compareCellLists(List<Cell> lCells1, List<Cell> lCells2) {
		boolean same = true;

		if(lCells1.size() != lCells2.size()) return false;
		
		List<Cell> l1 = new ArrayList<>(lCells1);
		List<Cell> l2 = new ArrayList<>(lCells2);
		
		Collections.sort(l1);
		Collections.sort(l2);

		for(int n=0; n < l1.size(); n++) {
			if(l1.get(n) != l2.get(n)) {
				same = false;
				break;
			}
		}
		
		return same;
	}
}
