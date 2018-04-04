package grid;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 *	Represents a Sudoku puzzle grid and the cells, rows, columns and boxes within it. 
 */

public class Grid {

	// The dimensions of the grid.
	private GridLayout m_layout;
	
	// The Rows, Columns, Boxes and Cells within the grid.
	private List<Column> m_lColumns;
	private List<Row> m_lRows;
	private List<Box> m_lBoxes;		
	private List<Cell> m_lCells;	
	
	public Grid(GridLayout layout) {
		m_layout = layout;

		// Add columns to the grid
		m_lColumns = new ArrayList<>();
		for(int columnNum = 0; columnNum < m_layout.m_columns; columnNum++) {
			m_lColumns.add(new Column(columnNum));
		}
			
		// Add rows to the grid
		m_lRows = new ArrayList<>();
		for(int rowNum = 0; rowNum < m_layout.m_rows; rowNum++) {
			m_lRows.add(new Row(rowNum));
		}
		
		// Add boxes to the grid
		m_lBoxes = new ArrayList<>();
		for(int boxNum = 0; boxNum < m_layout.m_boxes; boxNum++) {
			m_lBoxes.add(new Box(boxNum));
		}

		// Generate cells for the grid.
		m_lCells = new ArrayList<>();
		int cellNumber = 0;
		for(int rowNum = 0; rowNum < m_lRows.size(); rowNum++) {
			for(int columnNum = 0; columnNum < m_lColumns.size(); columnNum++) {
				addCell(cellNumber++, columnNum, rowNum);
			}			
		}		
	}

	// Create a new cell in the grid, including it in a specific column/row/box as well.
	private void addCell(int cellNumber, int columnNum, int rowNum) {
		Column column = m_lColumns.get(columnNum);
		Row row = m_lRows.get(rowNum);
		int boxNum = boxNumberFromGridPosition(columnNum, rowNum);
		Box box = m_lBoxes.get(boxNum);
		
		Cell cell = new Cell(cellNumber, column, row, box);
		m_lCells.add(cell);
		
		column.addCell(cell);
		row.addCell(cell);
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
	
	
	/**
	 * Check whether the assignments to the grid are compatible with each other.
	 *  
	 * @return A list of the cells in the grid which are not compatible, because a symbol appears more than once in a column, row or box. 
	 * If the grid is valid, an empty list will be returned.
	 */
	public List<Cell> listOfIncompatibleCells() {
		
		// Get a list of all the cellsets in the grid 
		List<CellSet> lCellSets = new ArrayList<>(m_lColumns);
		lCellSets.addAll(m_lRows);
		lCellSets.addAll(m_lBoxes);
		
		Set<Cell> s = new LinkedHashSet<>();	// Use a set for colecting the clashing cells, to prevent cells being listed more than once. 
		for(CellSet cellSet : lCellSets) {
			s.addAll(cellSet.listOfIncompatibleCells());
		}
		
		return new ArrayList<>(s);
	}
	
	// Work out cell and box numbering based on column and row numbering. Note that all
	// numbering uses the internal 0-based item numbers here.
	
	// Example 9x9 grid cell numbering
	// 0  1  ... 8
	// 9  10 ... 17
	// ..
	// 72 73 ... 80
	
	private int cellNumberFromGridPosition(int columnNumber, int rowNumber) {
		return (rowNumber * m_layout.m_columns) + columnNumber;
	}
	
	public Cell cellFromGridPosition(int columnNumber, int rowNumber) {		
		return m_lCells.get(cellNumberFromGridPosition(columnNumber, rowNumber));
	}
	
	// Example 9x9 grid box numbering
	// 0 1 2
	// 3 4 5
	// 6 7 8
	
	private int boxNumberFromGridPosition(int columnNumber, int rowNumber) {
		return ((rowNumber/m_layout.m_rowsPerBox) * m_layout.m_rowsPerBox) + 
			   (columnNumber / m_layout.m_columnsPerBox);
	}

	public Box boxFromGridPosition(int columnNumber, int rowNumber) {
		return m_lBoxes.get(boxNumberFromGridPosition(columnNumber, rowNumber));
	}
	
	// ---------------------------------------------------------------------
	//
	// Nested class to record some stats related to a parent grid and its cells and their assignments
	
	public class Stats {
		
		public GridLayout m_layout;
		public int m_initialAssignedCellCount;
		public int m_assignedCellCount;
		public int m_unassignedCellCount;
		
		private Stats() {
		}
	}
		
	public Stats stats() {
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
 