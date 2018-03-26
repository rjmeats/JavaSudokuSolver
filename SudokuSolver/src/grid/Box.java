package grid;

/**
 * Represents a 'box' of cells in a Sudoku grid - for example a 3x3 set of cells in a 9x9 grid.  
 */

public class Box extends CellSet {

	public Box(int boxNumber) {
		super("Box", boxNumber);
	}	
}

