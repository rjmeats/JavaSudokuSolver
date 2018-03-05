package solver;

import grid.Cell;

public interface CellContentProvider {

	String getHeading();
	String getContent(Cell c);
	boolean changedThisStep(Cell cell, int stepNumber);
}
