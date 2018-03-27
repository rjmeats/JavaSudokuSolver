package solver;

import grid.Cell;

public interface CellContentProvider {

	String getHeading();
	String getContent(Cell c);
	String getBasicCellClass();
	
	boolean staticContent();
	boolean changedThisStep(Cell cell, int stepNumber);
}
