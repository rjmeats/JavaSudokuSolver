package diagnostics;

import grid.Cell;

/**
 * Interface used when formatting grids for display.
 * 
 * Objects which implement the interface provide information for specific cells in the grid.
 */
public interface CellDiagnosticsProvider {

	/**
	 * Does the diagnostic content for a cell vary over time as a puzzle is filled in ? E.g. a cell identifier will stay constant
	 * but the assigned value will change.
	 * @return True if the diagnostic contents stay the same over time
	 */
	boolean hasStaticContent();
	
	/**
	 * Specifies the CSS style class name to use for the cell. 
	 * 
	 * Use if the default 'gridcell' class is not suitable (e.g. a large cell is needed).
	 * 
	 * @return The name of the style. Empty string if the gridcell class is OK to use.
	 */
	String getBasicCellClass();
	
	/**
	 * Specifies the diagnostic content to appear in the table representing the grid for a particular cell.
	 *  
	 * @param cell The grid cell whose value/state is to be represented
	 * @return A string representing the cell value/state
	 */
	String getCellDiagnostics(Cell cell);
	
	/**
	 * Has the diagnostic content to be displayed for this cell changed during the specified processing step ?
	 * @param cell The grid cell  
	 * @param stepNumber The processing step number
	 * @return True if the diagnostic content (value/state) to be displayed for the cell changed during the processing step 
	 */
	boolean changedThisStep(Cell cell, int stepNumber);
}
