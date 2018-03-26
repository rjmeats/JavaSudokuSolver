package grid;

/**
 * Represents a column of cells in a Sudoku grid - for example a 1x9 set of cells in a 9x9 grid.  
 */

public class Column extends CellSet {

	public Column(int columnNumber) {
		super("Column", columnNumber);
	}
}