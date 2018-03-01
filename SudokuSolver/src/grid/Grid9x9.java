package grid;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;

public class Grid9x9 {

	public List<Row> m_lRows;
	public List<Column> m_lColumns;
	public List<Box> m_lBoxes;	
	public List<Cell> m_lCells;
	public List<CellSet> m_lCellSets;
	
	Cell m_aCells[][];
	
	static int s_rows = 9;
	static int s_columns = 9;
	static int s_boxes = 9;
	
	public Grid9x9() {
		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellSets = new ArrayList<>();
		m_lCells = new ArrayList<>();
		m_aCells = new Cell[s_rows][s_columns];
		
		for(int rowNum = 0; rowNum < s_rows; rowNum++)
		{
			m_lRows.add(new Row(rowNum));
		}
		
		for(int columnNum = 0; columnNum < s_columns; columnNum++)
		{
			m_lColumns.add(new Column(columnNum));
		}
			
		for(int boxNum = 0; boxNum < s_boxes; boxNum++)
		{
			m_lBoxes.add(new Box(boxNum));
		}

		m_lCellSets = new ArrayList<>(m_lRows);
		m_lCellSets.addAll(m_lColumns);
		m_lCellSets.addAll(m_lBoxes);
		
		for(int rowNum = 0; rowNum < s_rows; rowNum++)
		{
			Row row = m_lRows.get(rowNum);
			for(int columnNum = 0; columnNum < s_columns; columnNum++)
			{
				Column column = m_lColumns.get(columnNum);

				int boxNum = getBoxNumberFromGridPosition(rowNum, columnNum);
				Box box = m_lBoxes.get(boxNum);
				Cell cell = new Cell(getCellNumberFromGridPosition(rowNum, columnNum), row, column, box);
				m_aCells[rowNum][columnNum] = cell;
				m_lCells.add(cell);
				row.addCell(cell);
				column.addCell(cell);
				box.addCell(cell);
			}			
		}		
	}

	public Set<Cell> isValid() {
		Set<Cell> l = new LinkedHashSet<>();
		
		// Check that each Cell set uses each symbol only once.
		for(CellSet cellSet : m_lCellSets) {
			l.addAll(cellSet.isValid());
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
	public static int getBoxNumberFromGridPosition(int rowNumber, int columnNumber) {
		return (rowNumber/3)*3 + columnNumber / 3;
	}
}
 