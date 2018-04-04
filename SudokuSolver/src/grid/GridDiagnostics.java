package grid;

import diagnostics.CellDiagnosticsProvider;

/**
 * Classes implementing the CellDiagnosticsProvider interface to provide different diagnostic grid displays.
 */

public abstract class GridDiagnostics implements CellDiagnosticsProvider {

	// Simple implementations for diagnostics that stay the same while a grid is being processed
	public boolean hasStaticContent() { return true;}
	public String basicCellClass() { return ""; }
	public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	public abstract String cellDiagnostics(Cell cell);
	
	/**
	 * Class to display the cell ID numbers in a grid
	 */
	public static class CellNumberDisplayer extends GridDiagnostics implements CellDiagnosticsProvider {
		
		public String cellDiagnostics(Cell cell) {
			return(cell.numberOnlyRepresentation());
		}
	}

	/**
	 * Class to display the cell location in a grid
	 */
	public static class CellLocationDisplayer extends GridDiagnostics implements CellDiagnosticsProvider {
		
		public String cellDiagnostics(Cell cell) {
			return(cell.gridLocation());
		}
	}

	/**
	 * Class to display the box number of a cell in a grid
	 */
	public static class BoxNumberDisplayer extends GridDiagnostics implements CellDiagnosticsProvider {
		
		public String cellDiagnostics(Cell cell) {
			return(cell.box().numberOnlyRepresentation());
		}
	}
	
	/**
	 * Class to display the value assigned to a cell in a grid, or a '.' if nothing assigned.
	 */
	public static class AssignedValueDisplay extends GridDiagnostics implements CellDiagnosticsProvider {

		@Override
		public boolean hasStaticContent() {
			return false;
		}

		@Override
		public boolean changedThisStep(Cell cell, int stepNumber) {
			return cell.isAssigned() && (cell.assignment().stepNumber() == stepNumber);
		}

		// Return the value assigned to the cell, or '.' if nothing yet assigned.
		public String cellDiagnostics(Cell cell) {
			return cell.isAssigned() ? cell.assignment().symbol().getRepresentation() : ".";
		}		
	}
}
