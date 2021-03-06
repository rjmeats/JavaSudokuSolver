package grid;

/**
 * Represent different size Sudoku grids and their layout properties. 
 */

public enum GridLayout {

	GRID9x9   ( 9,  9,  9,  3,  3),
	GRID16x16 (16, 16, 16,  4,  4),
	GRID25x25 (25, 25, 25,  5,  5),
	GRID6x6   ( 6,  6,  6,  3,  2);

	// Basic grid size properties
	
	public int m_columns;
	public int m_rows;
	public int m_boxes;
	public int m_columnsPerBox;
	public int m_rowsPerBox;
	
	public int m_cells;		// Derived.
	
	GridLayout(int columns, int rows, int boxes, int columnsPerBox, int rowsPerBox) {
		m_columns = columns;
		m_rows = rows;
		m_boxes = boxes;
		m_columnsPerBox = columnsPerBox;
		m_rowsPerBox = rowsPerBox;
		m_cells = m_rows * m_columns;
	}

	public String description() {
		return m_columns + "x" + m_rows;
	}
	
	public static GridLayout findGridLayoutOfSize(int rows) {
		GridLayout matchingGrid = null;
		for(GridLayout g : GridLayout.values()) {
			if(g.m_rows == rows) {
				matchingGrid = g;
				break;
			}
		}
			
		return matchingGrid;
	}
}
