package grid;

/**
 * Represent different size Sudoku grids and their layout properties. 
 */

public enum GridLayout {

	GRID9x9   ( 9,  9,  9,  3,  3),
	GRID16x16 (16, 16, 16,  4,  4),
	GRID25x25 (25, 25, 25,  5,  5),
	GRID6x6   ( 6,  6,  6,  2,  3);

	// Basic grid size properties
	
	public int m_rows;
	public int m_columns;
	public int m_boxes;
	public int m_rowsPerBox;
	public int m_columnsPerBox;
	
	public int m_cells;		// Derived.
	
	GridLayout(int rows, int columns, int boxes, int rowsPerBox, int columnsPerBox) {
		m_rows = rows;
		m_columns = columns;
		m_boxes = boxes;
		m_rowsPerBox = rowsPerBox;
		m_columnsPerBox = columnsPerBox;
		m_cells = m_rows * m_columns;
	}
}
