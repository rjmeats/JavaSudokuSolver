package diagnostics;

import grid.Grid9x9;
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
				boolean highlight = (cell.isAssigned() && (cell.getAssignment().getStepNumber() == stepNumberToHighlight));
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

}
