package grid;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

import puzzle.AssignmentMethod;

public class Grid9x9 {

	private List<Row> m_lRows;
	private List<Column> m_lColumns;
	private List<Box> m_lBoxes;	
	
	private List<Cell> m_lCells;	
	
	private static int s_rows = 9;
	private static int s_columns = 9;
	private static int s_boxes = 9;
	
	public Grid9x9() {

		m_lRows = new ArrayList<>();
		for(int rowNum = 0; rowNum < s_rows; rowNum++) {
			m_lRows.add(new Row(rowNum));
		}
		
		m_lColumns = new ArrayList<>();
		for(int columnNum = 0; columnNum < s_columns; columnNum++) {
			m_lColumns.add(new Column(columnNum));
		}
			
		m_lBoxes = new ArrayList<>();
		for(int boxNum = 0; boxNum < s_boxes; boxNum++) {
			m_lBoxes.add(new Box(boxNum));
		}

		m_lCells = new ArrayList<>();
		int cellNumber = 0;
		for(int rowNum = 0; rowNum < s_rows; rowNum++) {
			for(int columnNum = 0; columnNum < s_columns; columnNum++) {
				addCell(cellNumber++, rowNum, columnNum);
			}			
		}		
	}

	private void addCell(int cellNumber, int rowNum, int columnNum) {
		Row row = m_lRows.get(rowNum);
		Column column = m_lColumns.get(columnNum);
		int boxNum = getBoxNumberFromGridPosition(rowNum, columnNum);
		Box box = m_lBoxes.get(boxNum);
		
		Cell cell = new Cell(cellNumber, row, column, box);
		m_lCells.add(cell);
		
		row.addCell(cell);
		column.addCell(cell);
		box.addCell(cell);		
	}
	
	public List<Row> rows() {
		return Collections.unmodifiableList(m_lRows);
	}
	
	public List<Column> columns() {
		return Collections.unmodifiableList(m_lColumns);
	}
	
	public List<Box> boxes() {
		return Collections.unmodifiableList(m_lBoxes);
	}
	
	public List<Cell> cells() {
		return Collections.unmodifiableList(m_lCells);
	}
	
	public Set<Cell> getIncompatibleCells() {
		Set<Cell> l = new LinkedHashSet<>();
		
		// Get cells from each CellSet that use a symbol more than once.
		List<CellSet> lCellSets = new ArrayList<>(m_lRows);
		lCellSets.addAll(m_lColumns);
		lCellSets.addAll(m_lBoxes);
		
		for(CellSet cellSet : lCellSets) {
			l.addAll(cellSet.getIncompatibleCells());
		}
		
		return l;
	}
	
	// 0  1  ... 8
	// 9  10 ... 17
	// ..
	// 72 73 ... 80
	
	public static int getCellNumberFromGridPosition(int rowNumber, int columnNumber) {
		return rowNumber*s_columns + columnNumber;
	}
	
	public Cell getCellFromGridPosition(int rowNumber, int columnNumber) {
		return m_lCells.get(getCellNumberFromGridPosition(rowNumber, columnNumber));
	}
	
	// 0 1 2
	// 3 4 5
	// 6 7 8
	
	// Where do the hard-coded 3s come from. 9x9 obviously but how ????
	private static int getBoxNumberFromGridPosition(int rowNumber, int columnNumber) {
		return (rowNumber/3)*3 + columnNumber / 3;
	}

	public Box getBoxFromGridPosition(int rowNumber, int columnNumber) {
		return m_lBoxes.get(getBoxNumberFromGridPosition(rowNumber, columnNumber));
	}
	
	// ---------------------------------------------------------------------
	
	public class Stats {
		public int m_cellCount;
		public int m_initialAssignedCells;
		public int m_assignedCells;
		public int m_unassignedCells;		
		
		Stats() {
			m_cellCount = -1;
			m_initialAssignedCells = -1;
			m_assignedCells = -1;
			m_unassignedCells = -1;
		}
	}
	
	public Stats getStats() {
		Stats stats = new Stats();
		stats.m_cellCount = m_lCells.size();
		stats.m_initialAssignedCells = 0;
		stats.m_assignedCells = 0;
		stats.m_unassignedCells = 0;

		for(Cell cell : m_lCells) {
			if(cell.isAssigned()) {
				stats.m_assignedCells++;
				if(cell.assignment().method() == AssignmentMethod.Given) {
					stats.m_initialAssignedCells++;
				}
			}
			else {
				stats.m_unassignedCells++;
			}
		}
		return stats;
	}
}
 