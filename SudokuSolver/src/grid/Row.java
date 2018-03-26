package grid;

/**
 * Represents a row of cells in a Sudoku grid - for example a 9x1 set of cells in a 9x9 grid.  
 */

public class Row extends CellSet {

	public Row(int rowNumber) {
		super("Row", rowNumber);
	}	
}

