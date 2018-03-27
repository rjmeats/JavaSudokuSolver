package diagnostics;

import grid.Cell;

public interface CellDiagnosticsProvider {

	String getHeading();
	String getCellDiagnostics(Cell c);
	String getBasicCellClass();
	
	boolean hasStaticContent();
	boolean changedThisStep(Cell cell, int stepNumber);
}
