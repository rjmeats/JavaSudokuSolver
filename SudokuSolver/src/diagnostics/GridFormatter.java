package diagnostics;

import grid.Grid;
import grid.Cell;
import grid.AssignmentMethod;
import grid.Box;

public class GridFormatter {

	Grid m_grid;
	public GridFormatter(Grid grid) {
		m_grid = grid;
	}
	
	public String formatGrid(CellDiagnosticsProvider ccd) {
		return formatGrid(ccd, -1);
	}
	
	public String formatCompactGrid(CellDiagnosticsProvider ccd) {
		boolean compact = true;
		return formatGrid(ccd, -1, compact);
	}
	
	public String formatGrid(CellDiagnosticsProvider ccd, int stepNumberToHighlight) {
		boolean compact = false;
		return formatGrid(ccd, -1, compact);		
	}
	
	public String formatGrid(CellDiagnosticsProvider ccp, int stepNumberToHighlight, boolean compact) {
		Box currentHorizontalBox = null;
		Box currentVerticalBox = null;
		
		StringBuilder sb1 = new StringBuilder();
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			Box box = m_grid.getBoxFromGridPosition(0, rowNumber);
			if(box != currentVerticalBox) {
				if(currentVerticalBox!= null) {
					if(compact) {
						sb1.append("\r\n");
					}
					else {
						sb1.append("\r\n\r\n");						
					}
				}
				currentVerticalBox = box; 
			}

			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {
				box = m_grid.getBoxFromGridPosition(columnNumber, rowNumber);
				if(box != currentHorizontalBox) {
					if(compact) {
						sb1.append(" ");					}
					else {						
						sb1.append("    ");
					}
					currentHorizontalBox = box;
				}

				Cell cell = m_grid.getCellFromGridPosition(columnNumber, rowNumber);
				String contents = ccp.getCellDiagnostics(cell);
				if(compact) {
					sb1.append(contents.replaceAll("\\s+", " "));					
				}
				else {
					sb1.append(" " + contents + " ");
				}
			}
			
			sb1.append("\r\n");
		}
		
		return sb1.toString();
	}

	public String formatGridAsHTML(CellDiagnosticsProvider provider) {
		return formatGridAsHTML(provider, -1);
	}
	
	public String formatGridAsHTML(CellDiagnosticsProvider provider, int stepNumberToHighlight) {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		Box currentHorizontalBox = null;
		Box currentVerticalBox = null;

		sb.append("<table class=gridouter>").append(nl);
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			Box box = m_grid.getBoxFromGridPosition(0, rowNumber);
			String rowClass = "class=\"normalrow\"";
			if(box != currentVerticalBox) {
				if(currentVerticalBox != null) {
					rowClass = "class=\"gridseparatorrow\"";
				}
				currentVerticalBox = box;
			}
			sb.append("<tr " + rowClass + ">").append(nl);

			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {
				Cell cell = m_grid.getCellFromGridPosition(columnNumber, rowNumber);
				box = m_grid.getBoxFromGridPosition(columnNumber, rowNumber);
				String basicCellClass = provider.getBasicCellClass();
				if(basicCellClass.length() == 0) {
					basicCellClass = "gridcell";
				}
				String columnClass = basicCellClass + " gridnonseparatorcolumn";
				if(box != currentHorizontalBox) {
					if(columnNumber != 0) {
						columnClass = basicCellClass + " gridseparatorcolumn";
					}
				}
				currentHorizontalBox = box;

				String toolTip = cell.getGridLocationString() + " = " + cell.getRepresentation();
				if(cell.isAssigned()) {
					toolTip += " : " + cell.assignment().description();
				}
				
				boolean staticContent = provider.hasStaticContent();
				boolean highlight = provider.changedThisStep(cell,  stepNumberToHighlight);
				boolean given = cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given;
				if(staticContent) {
					
				}
				else if(highlight) {
					columnClass += " highlight";
				}
				else if(given) {
					columnClass += " given";					
				}
				else if(cell.isAssigned()) {
					columnClass += " previouslyassigned";
				}
				columnClass = "class=" + "\"" + columnClass + "\"";
				sb.append("<td " + columnClass + " title=\"" + toolTip + "\">").append(nl);
				String contents = provider.getCellDiagnostics(cell);
				sb.append(contents).append(nl);		// Protect
				
				sb.append("</td>").append(nl);
			}
			
			sb.append("</tr>").append(nl);
		}
		
		sb.append("</table>").append(nl);
		
		return sb.toString();
	}
}
