package diagnostics;

import grid.Grid9x9;
import puzzle.AssignmentMethod;
import grid.Cell;

import solver.CellContentProvider;

public class GridFormatter {

	Grid9x9 m_grid;
	public GridFormatter(Grid9x9 grid) {
		m_grid = grid;
	}
	
	public String formatGrid(CellContentProvider ccd) {
		return formatGrid(ccd, -1);
	}
	
	public String formatCompactGrid(CellContentProvider ccd) {
		boolean compact = true;
		return formatGrid(ccd, -1, compact);
	}
	
	public String formatGrid(CellContentProvider ccd, int stepNumberToHighlight) {
		boolean compact = false;
		return formatGrid(ccd, -1, compact);		
	}
	
	public String formatGrid(CellContentProvider ccp, int stepNumberToHighlight, boolean compact) {
		int currentHorizontalBoxNumber = -1;
		int currentVerticalBoxNumber = -1;
		
		StringBuilder sb1 = new StringBuilder();
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			int boxNumber = m_grid.getBoxFromGridPosition(rowNumber, 0).getBoxNumber();
			if(boxNumber != currentVerticalBoxNumber) {
				if(currentVerticalBoxNumber != -1) {
					if(compact) {
						sb1.append("\r\n");
					}
					else {
						sb1.append("\r\n\r\n");						
					}
				}
				currentVerticalBoxNumber = boxNumber; 
			}

			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {
				boxNumber = m_grid.getBoxFromGridPosition(rowNumber, columnNumber).getBoxNumber();
				if(boxNumber != currentHorizontalBoxNumber) {
					if(compact) {
						sb1.append(" ");					}
					else {						
						sb1.append("    ");
					}
					currentHorizontalBoxNumber = boxNumber;
				}

				Cell cell = m_grid.getCellFromGridPosition(rowNumber, columnNumber);
//				CellAssessment cell = getCellAssessmentForCell(c);
//				boolean highlight = (cell.isAssigned() && (cell.getAssignment().getStepNumber() == stepNumberToHighlight));
				String contents = ccp.getContent(cell);
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

	public String formatGridAsHTML(CellContentProvider provider) {
		return formatGridAsHTML(provider, -1);
	}
	
	public String formatGridAsHTML(CellContentProvider provider, int stepNumberToHighlight) {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		int currentHorizontalBoxNumber = -1;
		int currentVerticalBoxNumber = -1;

		sb.append("<table class=gridouter>").append(nl);
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			int boxNumber = m_grid.getBoxFromGridPosition(rowNumber, 0).getBoxNumber();
			String rowClass = "class=\"normalrow\"";
			if(boxNumber != currentVerticalBoxNumber) {
				if(currentVerticalBoxNumber != -1) {
					rowClass = "class=\"gridseparatorrow\"";
//					sb.append("<tr>");
//					for(int columnNumber = 0; columnNumber < m_grid.columns().size()+2; columnNumber++) {
//						sb.append("<td bgcolor=black></td>");						
//					}
//					sb.append("</tr>");
				}
				currentVerticalBoxNumber = boxNumber;
			}
			sb.append("<tr " + rowClass + ">").append(nl);

			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {
				Cell cell = m_grid.getCellFromGridPosition(rowNumber, columnNumber);
				boxNumber = m_grid.getBoxFromGridPosition(rowNumber, columnNumber).getBoxNumber();
				String basicCellClass = provider.getBasicCellClass();
				String columnClass = basicCellClass + " gridnonseparatorcolumn";
				if(boxNumber != currentHorizontalBoxNumber) {
					if(columnNumber != 0) {
//						sb.append("<td bgcolor=black></td>");						
						columnClass = basicCellClass + " gridseparatorcolumn";
					}
				}
				currentHorizontalBoxNumber = boxNumber;

				String toolTip = cell.getOneBasedGridLocationString() + " = " + cell.getOneBasedCellNumber();
				if(cell.isAssigned()) {
					toolTip += " : " + cell.assignment().toString();
				}
				boolean highlight = provider.changedThisStep(cell,  stepNumberToHighlight);
				boolean given = cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given;
				if(highlight) {
					columnClass += " highlight";
				}
				else if(given) {
					columnClass += " given";					
				}
				else if(cell.isAssigned()) {
					columnClass += " previouslyassigned";
				}
				columnClass = "class=" + "\"" + columnClass + "\"";
//				String colour = highlight ? " bgcolor=cyan" : " bgcolor=ivory";
				sb.append("<td " + columnClass + " title=\"" + toolTip + "\">").append(nl);
				String contents = provider.getContent(cell);
				sb.append(contents).append(nl);		// Protect
				
				sb.append("</td>").append(nl);
			}
			
			sb.append("</tr>").append(nl);
		}
		
		sb.append("</table>").append(nl);
		
		return sb.toString();
	}
}
