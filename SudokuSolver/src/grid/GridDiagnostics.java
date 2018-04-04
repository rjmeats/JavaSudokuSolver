package grid;

import diagnostics.CellDiagnosticsProvider;

public class GridDiagnostics {

	public static class CellNumberDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(GridDiagnostics.padRight(cell.getNumberOnlyRepresentation(), 5));
		}
		public String getBasicCellClass() {
			return "";
		}
		
		public boolean hasStaticContent() { return true;}
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class CellLocationDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(GridDiagnostics.padRight(cell.getGridLocationString(), 5));
		}
		public String getBasicCellClass() {
			return "";
		}
		
		public boolean hasStaticContent() { return true; }
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class BoxNumberDisplayer implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			return(GridDiagnostics.padRight(cell.box().getNumberOnlyRepresentation(), 5));
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
			return(GridDiagnostics.padRight(representation, 5));
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

	public static String padRight(String s, int width, char padChar)
	{	
		StringBuilder sb = new StringBuilder();
		sb.append((s == null) ? "" : s);
		
		while(sb.length() < width)
		{
			sb.append(padChar);
		}
		
		return sb.toString();
	}

	public static String padRight(String s, int width)
	{
		return padRight(s, width, ' ');
	}

	public static String padRight(int n, int width)
	{
		String s = Integer.toString(n);
		return padRight(s, width, ' ');
	}
}


