package diagnostics;

import grid.Cell;

public interface CellDiagnosticsProvider {

	String getCellDiagnostics(Cell c);
	String getBasicCellClass();
	
	boolean hasStaticContent();
	boolean changedThisStep(Cell cell, int stepNumber);
}