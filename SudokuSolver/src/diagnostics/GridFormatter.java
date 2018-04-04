package diagnostics;

import grid.Grid;
import grid.Cell;
import grid.AssignmentMethod;
import grid.Box;

/**
 * Produces formatted versions of a Sudoku grid.
 * 
 * Formats which can be produced:
 *
 * - simple compact text-based grid
 * - simple text-based grid with more space between cells
 * - an HTML grid
 * 
 * Each formatting method uses an object which implements the CellDiagnosticsProvider interface to
 * provide cell content. This can be a cell value, or other diagnostic information relating to a cell.
 */

public class GridFormatter {

	private static String nl = System.lineSeparator();		// Newline character(s) for this environment
	
	private Grid m_grid;
	
	public GridFormatter(Grid grid) {
		m_grid = grid;
	}

	/**
	 * Produce a compact representation of a grid.
	 * 
	 * Something like this:
	 * 
	 *  4 5 8  9 7 2  3 1 6 
	 *  2 6 3  5 4 1  8 7 9 
	 *  1 7 9  6 3 8  2 4 5
	 *   
	 *  5 4 1  7 8 6  9 3 2 
	 *  9 3 7  1 2 5  4 6 8 
	 *  6 8 2  3 9 4  7 5 1 
	 *  
	 *  7 2 6  4 1 9  5 8 3 
	 *  3 9 5  8 6 7  1 2 4 
	 *  8 1 4  2 5 3  6 9 7 

	 * @param ccp Object implementing the CellDiagnosticsProvider to provide cell content  information
	 * @return String containing the formatted grid
	 */
	public String formatCompactGrid(CellDiagnosticsProvider ccp) {
		boolean compact = true;
		return formatGrid(ccp, compact);
	}
	
	/**
	 * Produce a compact representation of a grid.
	 * 
	 * Something like this:
	 * 
	 *  4      5      8          9      7      2          3      1      6     
	 *  2      6      3          5      4      1          8      7      9      
	 *  1      7      9          6      3      8          2      4      5     
	 *   
	 *   
	 *  5      4      1          7      8      6          9      3      2      
	 *  9      3      7          1      2      5          4      6      8      
	 *  6      8      2          3      9      4          7      5      1      
	 *  
	 *  
	 *  7      2      6          4      1      9          5      8      3     
	 *  3      9      5          8      6      7          1      2      4     
	 *  8      1      4          2      5      3          6      9      7
	 *       
	 * @param ccp Object implementing the CellDiagnosticsProvider to provide cell content  information
	 * @return String containing the formatted grid
	 */
	public String formatGrid(CellDiagnosticsProvider ccp) {
		boolean compact = false;
		return formatGrid(ccp, compact);		
	}

	// Do the detailed formatting to produce a text-based grid.
	private String formatGrid(CellDiagnosticsProvider ccp, boolean compact) {
		
		// Keep track of which box in the grid we've reached, to allow us to put
		// extra space between boxes.		
		Box currentHorizontalBox = null;
		Box currentVerticalBox = null;
		
		StringBuilder sb = new StringBuilder();
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			
			// Add blank line(s) between vertical boxes
			Box box = m_grid.boxFromGridPosition(0, rowNumber);
			if((currentVerticalBox != null) && (currentVerticalBox != box)) {
				sb.append(compact ? nl : nl+nl);
			}
			currentVerticalBox = box; 

			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {
				// Add blank column(s) between horizontal boxes
				box = m_grid.boxFromGridPosition(columnNumber, rowNumber);
				if(currentHorizontalBox != box) {
					sb.append(compact ? " " : "   ");
				}
				currentHorizontalBox = box;

				// Put the required value for the cell into the formatted grid. 
				Cell cell = m_grid.cellFromGridPosition(columnNumber, rowNumber);
				String contents = ccp.cellDiagnostics(cell);
				if(compact) {
					// Single space between items. Only really works if the contents provided are the same number of
					// characters within a particular grid.
					sb.append(contents.replaceAll("\\s+", "").trim()).append(" "); 
				}
				else {
					sb.append(contents + "  ");	
				}
			}
			
			sb.append(nl);
		}
		
		return sb.toString();
	}

	// Do the detailed formatting to produce an HTML table. Uses various classes for its table elements
	// to control how the grid lines appear (found by trial and error, may well be a better way):
	//
	// - gridouter : table class
	//
	// - normalrow or gridseparatorrow : used for each row. 
	//   - gridseparatorrow is used if this row is at the top of a box (other than the top row)
	//
	// - gridcell or a class provided by the CellDiagnosticsProvider : provides basic cell formatting 
	// - gridseparatorcolumn or gridnonseparatorcolumn : added to the basic cell class to control vertical box boundaries,
	//      with gridseparatorcolumn used if this column is at the start of a box (other than the leftmost column)
	//  - cells can then have one of three additional classes added:
	//    - given : cell value was provided with the initial Sudoku grid
	//    - changed : the cell has just changed with the latest processing step
	//    - solved : the cell value was worked out in an earlier step
	//
	// CSS styles for use in an HTML page header are provided by the function following this
		
	public String formatGridAsHTML(CellDiagnosticsProvider provider) {
		return formatGridAsHTML(provider, -1);
	}
	
	public String formatGridAsHTML(CellDiagnosticsProvider provider, int stepNumberToHighlight) {

		// Keep track of which box we've reached in the grid, so we can adjust the cell borders.
		Box currentHorizontalBox = null;
		Box currentVerticalBox = null;

		StringBuilder sb = new StringBuilder();
		sb.append("<table class=gridouter>").append(nl);
		
		for(int rowNumber = 0; rowNumber < m_grid.rows().size(); rowNumber++) {
			
			// Set the row class to differentiate rows where a new box starts. 
			Box box = m_grid.boxFromGridPosition(0, rowNumber);
			String rowClass = "normalrow";
			if(box != currentVerticalBox) {
				if(currentVerticalBox != null) {
					rowClass = "gridseparatorrow";		// Need to use a different top border
				}
			}
			currentVerticalBox = box;
			sb.append("<tr class=\"" + rowClass + "\">").append(nl);
			
			for(int columnNumber = 0; columnNumber < m_grid.columns().size(); columnNumber++) {

				// Set the class(es) for each cell in the grid/table. This has three aspects:
				// - the basic cell size, defaulting to gridcell class, but can be overridden by the diagnostics provider
				// - the cell border class to allow vertical borders between boxes to be shown
				// - background colouring to indicate more details of a cell's processing
				
				String basicCellClass = provider.basicCellClass();
				if(basicCellClass.length() == 0) {
					basicCellClass = "gridcell";
				}
				
				box = m_grid.boxFromGridPosition(columnNumber, rowNumber);
				String cellClass = basicCellClass + " gridnonseparatorcolumn";
				if(box != currentHorizontalBox) {
					if(columnNumber != 0) {
						cellClass = basicCellClass + " gridseparatorcolumn";	// Produce a box border on the left of the cell
					}
				}
				currentHorizontalBox = box;

				// Set background colour for the cell
				Cell cell = m_grid.cellFromGridPosition(columnNumber, rowNumber);				
				if(provider.hasStaticContent()) {
					// Values for a cell are always the same (e.g. the cell identifier), no highlighting
				}
				else if(provider.changedThisStep(cell, stepNumberToHighlight)) {
					// The contents shown in this cell changed in the current processing step
					cellClass += " changed";
				}
				else if(cell.isAssigned() && (cell.assignment().method() == AssignmentMethod.Given)) {
					// We were given this cell's value at the start of the puzzle.
					cellClass += " given";					
				}
				else if(cell.isAssigned()) {
					// We worked out what the value of this cell was in an earlier step.
					cellClass += " solved";
				}
				String fullCellClass = "class=" + "\"" + cellClass + "\"";
				
				// Also generate text to be shown in the mouse lingers over the cell, identifying the cell, 
				// and providing assignment info.
				String toolTip = cell.gridLocation() + " = " + cell.getRepresentation();
				if(cell.isAssigned()) {
					toolTip += " : " + cell.assignment().description();
				}
				
				sb.append("<td " + fullCellClass + " title=\"" + toolTip + "\">").append(nl);
				
				// Get the detailed contents from the diagnostics provder and add to the HTML.
				String contents = provider.cellDiagnostics(cell);
				sb.append(protectHTML(contents)).append(nl);
				
				sb.append("</td>").append(nl);
			}
			
			sb.append("</tr>").append(nl);
		}
		
		sb.append("</table>").append(nl);
		
		return sb.toString();
	}
	
	public static String CSSStyles() {
		StringBuilder sb = new StringBuilder();
		
		// Font
		sb.append("body {").append(nl);
		sb.append("    font-family: Tahoma, Geneva, sans-serif;").append(nl);			// https://www.w3schools.com/cssref/css_websafe_fonts.asp
		sb.append("}").append(nl);
		
		// Drawing boxes in the grid
		sb.append(".gridouter {").append(nl);
		sb.append("    border: 6px solid gray;").append(nl);		// External table border
		sb.append("    border-collapse: collapse;").append(nl);		
		sb.append("}").append(nl);
		sb.append(".gridseparatorrow {").append(nl);
		sb.append("    border-top: 4px solid grey;").append(nl);	// Internal box border
		sb.append("}").append(nl);
		sb.append(".gridseparatorcolumn {").append(nl);
		sb.append("    border-left: 4px solid grey;").append(nl);	// Internal box border
		sb.append("    border-right: 1px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("}").append(nl);
		sb.append(".gridnonseparatorcolumn {").append(nl);
		sb.append("    border-left: 1px solid grey;").append(nl);
		sb.append("    border-right: 1px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("}").append(nl);
		
		// Shape/size of the grid cell
		sb.append(".gridcell {").append(nl);
		sb.append("    width: 20px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    text-align: center;").append(nl);
		sb.append("}").append(nl);

		// Cell background colouring
		sb.append(".changed {").append(nl);
		sb.append("    background-color: yellow;").append(nl);
		sb.append("}").append(nl);
		sb.append(".given {").append(nl);
		sb.append("    background-color: lightsteelblue;").append(nl);
		sb.append("}").append(nl);
		sb.append(".solved {").append(nl);
		sb.append("    background-color: aquamarine;").append(nl);
		sb.append("}").append(nl);

		return sb.toString();
	}

	/**
	 * Protect special HTML characters.
	 * 
	 * @param str Original String
	 * @return Protected String
	 */
	public static String protectHTML(String str) {
		String	protectedString = str;
		protectedString = protectedString.replaceAll ("&", "&amp;");	// Must be first
		protectedString = protectedString.replaceAll ("\"", "&#34;");
		protectedString = protectedString.replaceAll ("<", "&lt;");
		protectedString = protectedString.replaceAll ("<", "&gt;");		
		return (protectedString);
	}
}
