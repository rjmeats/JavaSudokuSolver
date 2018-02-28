import java.util.List;
import java.util.ArrayList;

public class Grid {

	List<Row> m_lRows;
	List<Column> m_lColumns;
	List<Box> m_lBoxes;	
	List<Cell> m_lCells;
	List<CellSet> m_lCellSets;
	
	Cell m_aCells[][];
	
	static int s_rows = 9;
	static int s_columns = 9;
	static int s_boxes = 9;
	
	List<Symbol> m_lSymbols;
	
	Grid() {
		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellSets = new ArrayList<>();
		m_lCells = new ArrayList<>();
		m_aCells = new Cell[s_rows][s_columns];
		m_lSymbols = getListOfSymbols(s_rows);
		
		for(int rowNum = 0; rowNum < s_rows; rowNum++)
		{
			m_lRows.add(new Row(rowNum, m_lSymbols));
		}
		
		for(int columnNum = 0; columnNum < s_columns; columnNum++)
		{
			m_lColumns.add(new Column(columnNum, m_lSymbols));
		}
			
		for(int boxNum = 0; boxNum < s_boxes; boxNum++)
		{
			m_lBoxes.add(new Box(boxNum, m_lSymbols));
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
	
	static List<Symbol> getListOfSymbols(int n)
	{
		// Need to check n ????
		List<Symbol> l = new ArrayList<>();
		for(Symbol cs : Symbol.values())
		{
			l.add(cs);
		}
		
		return l;
	}

	// 0  1  ... 8
	// 9  10 ... 17
	// ..
	// 72 73 ... 80
	
	static int getCellNumberFromGridPosition(int rowNumber, int columnNumber) {
		return rowNumber*9 + columnNumber % 9;
	}
	
	// 0 1 2
	// 3 4 5
	// 6 7 8
	static int getBoxNumberFromGridPosition(int rowNumber, int columnNumber) {
		return (rowNumber/3)*3 + columnNumber / 3;
	}
	
	
}
 