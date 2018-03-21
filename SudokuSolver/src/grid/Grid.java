package grid;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

public class Grid {

	private GridLayout m_layout;
	private List<Row> m_lRows;
	private List<Column> m_lColumns;
	private List<Box> m_lBoxes;		
	private List<Cell> m_lCells;	
	
	public Grid(GridLayout layout) {

		m_layout = layout;
		
		m_lRows = new ArrayList<>();
		for(int rowNum = 0; rowNum < m_layout.m_rows; rowNum++) {
			m_lRows.add(new Row(rowNum));
		}
		
		m_lColumns = new ArrayList<>();
		for(int columnNum = 0; columnNum < m_layout.m_columns; columnNum++) {
			m_lColumns.add(new Column(columnNum));
		}
			
		m_lBoxes = new ArrayList<>();
		for(int boxNum = 0; boxNum < m_layout.m_boxes; boxNum++) {
			m_lBoxes.add(new Box(boxNum));
		}

		m_lCells = new ArrayList<>();
		int cellNumber = 0;
		for(int rowNum = 0; rowNum < m_lRows.size(); rowNum++) {
			for(int columnNum = 0; columnNum < m_lColumns.size(); columnNum++) {
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
	
	public GridLayout layout() {
		return m_layout;
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
	
	public List<Cell> getListOfIncompatibleCells() {
		
		// Get a list of all the cells which clash.
		List<CellSet> lCellSets = new ArrayList<>(m_lRows);
		lCellSets.addAll(m_lColumns);
		lCellSets.addAll(m_lBoxes);
		
		Set<Cell> s = new LinkedHashSet<>();	// Only list each clashing cell once. 
		for(CellSet cellSet : lCellSets) {
			s.addAll(cellSet.getListOfIncompatibleCells());
		}
		
		return new ArrayList<>(s);
	}
	
	// 0  1  ... 8
	// 9  10 ... 17
	// ..
	// 72 73 ... 80
	
	private int getCellNumberFromGridPosition(int rowNumber, int columnNumber) {
		return rowNumber * m_layout.m_columns + columnNumber;
	}
	
	public Cell getCellFromGridPosition(int rowNumber, int columnNumber) {		
		return m_lCells.get(getCellNumberFromGridPosition(rowNumber, columnNumber));
	}
	
	// 0 1 2
	// 3 4 5
	// 6 7 8
	
	private int getBoxNumberFromGridPosition(int rowNumber, int columnNumber) {
		return ((rowNumber/m_layout.m_rowsPerBox) * m_layout.m_rowsPerBox) + 
			   (columnNumber / m_layout.m_columnsPerBox);
	}

	public Box getBoxFromGridPosition(int rowNumber, int columnNumber) {
		return m_lBoxes.get(getBoxNumberFromGridPosition(rowNumber, columnNumber));
	}
	
	// ---------------------------------------------------------------------
	
	public class Stats {
		
		public GridLayout m_layout;
		public int m_initialAssignedCellCount;
		public int m_assignedCellCount;
		public int m_unassignedCellCount;
		
		private Stats() {
		}
	}
	
	public Stats getStats() {
		Stats stats = new Stats();
		stats.m_layout = m_layout;
		stats.m_initialAssignedCellCount = 0;
		stats.m_assignedCellCount = 0;
		stats.m_unassignedCellCount = 0;

		for(Cell cell : m_lCells) {
			if(cell.isAssigned()) {
				stats.m_assignedCellCount++;
				if(cell.assignment().method() == AssignmentMethod.Given) {
					stats.m_initialAssignedCellCount++;
				}
			}
			else {
				stats.m_unassignedCellCount++;
			}
		}
		return stats;
	}
}
 