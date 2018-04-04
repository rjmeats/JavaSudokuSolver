package grid;

import diagnostics.CellDiagnosticsProvider;
import diagnostics.FormatUtils;

public class GridDiagnostics {

	public static class CellNumberDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(FormatUtils.padRight(cell.getNumberOnlyRepresentation(), 5));
		}
		public String getBasicCellClass() {
			return "";
		}
		
		public boolean hasStaticContent() { return true;}
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class CellLocationDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(FormatUtils.padRight(cell.getGridLocationString(), 5));
		}
		public String getBasicCellClass() {
			return "";
		}
		
		public boolean hasStaticContent() { return true; }
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class BoxNumberDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(FormatUtils.padRight(cell.box().getNumberOnlyRepresentation(), 5));
		}

		public String getBasicCellClass() {
			return "";
		}
		
		public boolean hasStaticContent() { return true; }
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}
	
	public static class AssignedValueDisplay implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			String representation = ".";
			if(cell.isAssigned())
			{
				Symbol symbol = cell.assignment().symbol();
				representation = symbol.getRepresentation();
			}
			return(FormatUtils.padRight(representation, 5));
		}
		
		public boolean changedThisStep(Cell cell, int stepNumber) {
			return cell.isAssigned() && (cell.assignment().stepNumber() == stepNumber);
		}

		public boolean hasStaticContent() {
			return false;
		}

		public String getBasicCellClass() {
			return "";
//			return "gridcell";
		}
	}
	

}
