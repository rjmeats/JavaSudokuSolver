package grid;

public enum GridLayout {

	GRID9x9(9, 9, 9, 3, 3),
	GRID16x16(16, 16, 16, 4, 4),
	GRID(6,6,6, 2, 3);
	
	int m_rows;
	int m_columns;
	int m_boxes;
	int m_rowsPerBox;
	int m_columnsPerBox;
	
	GridLayout(int rows, int columns, int boxes, int rowsPerBox, int columnsPerBox) {
		m_rows = rows;
		m_columns = columns;
		m_boxes = boxes;
		m_rowsPerBox = rowsPerBox;
		m_columnsPerBox = columnsPerBox;
	}
}
