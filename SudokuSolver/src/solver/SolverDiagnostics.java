package solver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import grid.AssignmentMethod;
import grid.Cell;
import grid.Grid;
import grid.Symbol;
import grid.Symbols;
import grid.GridDiagnostics;

import diagnostics.CellDiagnosticsProvider;
import diagnostics.GridFormatter;

/**
 * Class to generate an HTML of detailed diagnostics information generated from trying to solve a puzzle. 
 */

class SolverDiagnostics {

	private static String nl = System.lineSeparator();

	// Information about the puzzle being solved
	private Grid m_grid;
	private Symbols m_symbols;
	private String m_puzzleSource;

	// The solver object doing the solving.
	private Solver m_solver;

	// HTML diagnostics built up during a solution run. 
	private String m_htmlDiagnostics;
	
	// How often to generate detailed diagnostics information. For smaller grids, generate detailed diagnostics after each
	// processing step, but for larger grids this produces massive HTML pages, so only do the full details every n steps.
	private int m_diagnosticsFrequency;

	SolverDiagnostics(Solver solver, Grid grid, Symbols symbols, String puzzleSource) {
		m_solver = solver;
		m_grid = grid;
		m_symbols = symbols;
		m_puzzleSource = puzzleSource;
		
		m_htmlDiagnostics = "";
		m_diagnosticsFrequency = m_grid.cells().size() > 100 ? 10 : 1;	// Restrict diagnostics for grids larger than 9x9
		
		produceInitialDiagnostics();
	}

	// Convenience shortened reference to method to protect <, >, etc in HTML text
	static String protect(String s) {
		return GridFormatter.protectHTML(s);
	}
	
	// Produce a full HTML page by combining the diagnostics recorded so far with outer <html>, <head>, <body> elements, including
	// CSS styles in the <head> element.
	String htmlDiagnostics() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(nl);
		sb.append("<head>").append(nl);
		sb.append("<title>").append(protect(m_puzzleSource)).append("</title>").append(nl);;
		sb.append("<style>").append(nl);
		sb.append(GridFormatter.CSSStyles()).append(nl);
		sb.append(SolverDiagnostics.diagnosticStyles()).append(nl);
		sb.append("</style>").append(nl);
		sb.append("</head>").append(nl);
		sb.append("<body>").append(nl);
		sb.append(m_htmlDiagnostics).append(nl);
		sb.append("</body>").append(nl);
		sb.append("</html>").append(nl);

		return sb.toString();
	}
	
	// -----------------------------------------------------------------------------------------
	
	// Produce CSS styles for styles specific to this module's HTML. (GridFormatter styles are included separately.)
	
	private static String diagnosticStyles() {
		StringBuilder sb = new StringBuilder();
		
		// Size of table cells used to show information about values which could be assigned to a Sudoku cell. These need
		// to be quite wide as a list of several symbols may need to be shown.
		sb.append(".couldbevaluecell {").append(nl);
		sb.append("    width: 100px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    text-align: center;").append(nl);
		sb.append("}").append(nl);
		
		// CSS styles relating to the tables of cellsets
		sb.append(".cellsettable {").append(nl);
		sb.append("    border: 6px solid gray;").append(nl);
		sb.append("    border-collapse: collapse;").append(nl);
		sb.append("}").append(nl);
		// The righthand column showing a title for each row in the table
		sb.append(".cellsettabletitle {").append(nl);
		sb.append("    font-weight: bold;").append(nl);
		sb.append("    width: 80px;").append(nl);
		sb.append("    border-left: 1px solid grey;").append(nl);
		sb.append("    border-right: 2px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("}").append(nl);
		sb.append(".cellsetcell {").append(nl);
		sb.append("    border-left: 1px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("}").append(nl);
		// Display any errors
		sb.append(".observation { color: red; }").append(nl);
		// Other
		sb.append(".basictable {").append(nl);
		sb.append("    border-collapse: collapse;").append(nl);
		sb.append("    border-left: 2px solid grey;").append(nl);
		sb.append("    border-right: 2px solid grey;").append(nl);
		sb.append("    border-top: 2px solid grey;").append(nl);
		sb.append("    border-bottom: 2px solid grey;").append(nl);
		sb.append("}").append(nl);

		return sb.toString();
	}

	// -----------------------------------------------------------------------------------------

	// Produce diagnostics at the start of the puzzle-solving process showing:
	// - general info
	// - the starting grid
	// - some static properties of the grid cells
	
	private void produceInitialDiagnostics() {
		StringBuilder sb = new StringBuilder();
		
		// Show where the Sudoku came from, date/time of this run and what the different shading means. Wrap this
		// up in an HTML table.
		
		sb.append("<table>").append(nl);

		sb.append("<tr>").append(nl);
		sb.append("<td width=200>Sudoku source:</td><td>").append(protect(m_puzzleSource)).append("</td>").append(nl);
		sb.append("</tr>").append(nl);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
		String dateTime = dateFormat.format(m_solver.startTime());	// Local time
		sb.append("<tr>").append(nl);
		sb.append("<td>Run at:</td><td>").append(protect(dateTime)).append("</td>").append(nl);
		sb.append("</tr>").append(nl);
		
		sb.append("<tr>").append(nl);
		sb.append("<td>Shading key:</td><td>").append(nl);

		// Inner table of shadings 
		{
			sb.append("<table>").append(nl);
			sb.append("<tr>").append(nl);
			sb.append("<td class=given>Given</td>").append(nl);
			sb.append("<td class=changed>Changed</td>").append(nl);
			sb.append("<td class=solved>Solved</td>").append(nl);
			sb.append("</tr>").append(nl);
			sb.append("</table>").append(nl);
		}
		sb.append("</td></tr>").append(nl);
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		// Show four versions of the grid next to each other, within a 1x4 outer table
		// - showing the initial 'given' values for the grid
		// - showing static cell numbering
		// - showing static cell location references
		// - showing static cell box numbers
		
		sb.append("<h2>Initial Grid" + "</h2>").append(nl);			

		GridFormatter formatter = new GridFormatter(m_grid);		
		
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		
		sb.append("<td>Given values<p/>").append(nl);
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay()));
		sb.append("</td>").append(nl);
		
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>").append(nl);	// Spacing
		
		sb.append("<td>Cell numbering<p/>").append(nl);
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.CellNumberDisplayer()));
		sb.append("</td>").append(nl);
		
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>").append(nl);	// Spacing
		
		sb.append("<td>Cell locations<p/>").append(nl);
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.CellLocationDisplayer()));
		sb.append("</td>").append(nl);
		
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>").append(nl);	// Spacing
		
		sb.append("<td>Box numbering<p/>").append(nl);
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.BoxNumberDisplayer()));
		sb.append("</td>").append(nl);
		
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	// -----------------------------------------------------------------------------------------

	// Produce diagnostics after a solution processing step showing:
	// - whether any change-of-state action was performed
	// - the values currently assigned to cells in the grid
	// - what 'could be' values relate to unassigned cells in the grid
	// - what cells could relate to each symbol in each cellset
	//
	// Full diagnostics production may be suppressed for the step if this is a large grid.
	
	void produceDiagnosticsAfterStep(int stepNumber, boolean changedState, List<String> actions, List<String> stepObservations, boolean forceDiagnostics) {
		
		StringBuilder sb = new StringBuilder();
		
		// Step number -1 indicates that we've not made any deductions, just applied the information from the initial grid.
		if(stepNumber == -1) {
			sb.append("<h2>Initial Grid Assessment" + "</h2>").append(nl);			
		}
		else {
			sb.append("<h2>Grid Assessment after Step " + stepNumber + "</h2>").append(nl);
		}

		sb.append("<p/>");
		// If there are any errors recorded (probably indicating a bug), show them as the 'observation' CSS style 
		for(String obs : stepObservations) {
			sb.append("<div class=observation>" + protect(obs) + "</div>").append("<p/>").append(nl);
		}
		
		// Show the action(s) performed during the current step as a list.
		sb.append("<ul>");
		for(String a : actions) {
			sb.append("<li>").append(protect(a)).append("</li>").append(nl);
		}
		sb.append("</ul>");

		if(!changedState) {
			// We've got stuck
			sb.append("No state changes caused by this step").append("<p/>");
		}		
		else if((stepNumber % m_diagnosticsFrequency != 0) && (stepNumber != -1) && (!forceDiagnostics)) {
			// Stop output volume becoming excessive for larger grid sizes, but always put out diagnostics
			// for the initial processing situation and if told to (because the deduction was a complex one).
		}
		else {
			// Show detailed grid diagnostics for this step.
			//
			// Firstly show three grids next to each other (within an outer 1x3 table) showing:
			// - assigned values
			// - how many symbols could still be assigned to unassigned cells
			// - what those symbols are
			GridFormatter formatter = new GridFormatter(m_grid);
			
			sb.append("<table>").append(nl);
			sb.append("<tr>").append(nl);
			
			sb.append("<td>Assigned values<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);		// Spacing
			
			sb.append("<td>Could-be values count<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new SolverDiagnostics.CouldBeValueCountDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);		// Spacing
			
			sb.append("<td>Could-be values list<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new SolverDiagnostics.CouldBeValueDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			
			sb.append("</tr>").append(nl);
			sb.append("</table>").append(nl);
			
			// Now show information about each cellset. We can't use the GridFormatter for this, we need to set up the 
			// HTML table ourselves here. The HTML table has a row for each cellset and a column for each possible symbol;
			// the elements of the table show which cells within the cellset could still be assigned to the symbol. 
			
			sb.append("Possible cells for each symbol in each row/column/box");
			sb.append("<p/>").append(nl);
	
			Set<Symbol> symbols = m_symbols.symbolSet();
			
			sb.append("<table class=cellsettable>").append(nl);
			// We have two heading rows
			// Heading row 1 - just put the heading 'Symbol' at the top of the set of Symbol columns, so only 2 columns
			sb.append("<tr>").append(nl);
			sb.append("<th class=\"cellsetcell cellsettabletitle\">").append("").append("</th>").append(nl);
			sb.append("<th class=\"cellsetcell cellsettabletitle\" colspan=" + symbols.size() + ">").append("Symbol").append("</th>").append(nl);
			sb.append("</tr>").append(nl);

			// Heading row two has a column heading for the cellsets and one heading per symbol.
			sb.append("<tr>").append(nl);
			sb.append("<th class=\"cellsetcell cellsettabletitle\">").append("").append("</th>").append(nl);
			for(Symbol symbol : symbols) {
				sb.append("<th class=\"cellsetcell cellsettabletitle\">").append(protect(symbol.getRepresentation())).append("</th>").append(nl);			
			}
			sb.append("</tr>").append(nl);
			
			// Now we have the data rows, one per cellset. We have three types of cellset to work through, Rows, Columns and Boxes
			for(int cellSet = 1; cellSet <= 3; cellSet++) {
				for(CellSetAssessment cellset : m_solver.cellSetAssessments()) {
					if((cellSet == 1) && !(cellset instanceof RowAssessment)) continue;
					if((cellSet == 2) && !(cellset instanceof ColumnAssessment)) continue;
					if((cellSet == 3) && !(cellset instanceof BoxAssessment)) continue;

					// Show the cell set name in the first column, highlighting it if the cellset assessment changed in the latest step
					String cls = "cellsetcell cellsettabletitle";										
					if(cellset.stepNumberOfLatestChange() == stepNumber) {
						cls += " changed";
					}					
					String clsAttribute = "class=\"" + cls + "\"";
					sb.append("<tr>").append(nl);					
					sb.append("<td " + clsAttribute + ">").append(protect(cellset.getRepresentation())).append("</td>").append(nl);
					
					// Show information for each symbol in the cellset
					for(Symbol symbol : symbols) {
						cls = "cellsetcell";
						// Has the set of cells for this symbol changed in the latest step ?
						boolean changed = (cellset.stepNumberOfLatestChangeForSymbol(symbol) == stepNumber);
						if(changed) {
							cls += " changed";
						}
						
						// Get a list of the cells which could still be assigned to this symbol. 
						List<Cell> lc = new ArrayList<>(cellset.couldBeCellsForSymbol(symbol));
						// If there's only one cell possible, work out possible highlighting needed. 
						if(lc.size() == 1) {
							Cell cell = lc.get(0);
							if(changed) {
								// We're already set up 'changed' highlighting above
							}
							else if(cell.isAssigned() && (cell.assignment().method() == AssignmentMethod.Given)) {
								// This cell/symbol were provided as part of the initial puzzle.
								cls += " given";					
							}
							else if(cell.isAssigned()) {
								// We made this assignment during a previous step.
								cls += " solved";
							}
						}
						
						clsAttribute = "class=\"" + cls + "\"";
						// Set up a tooltip to show when the set of cells last changed
						String title = "lastchange=" + cellset.stepNumberOfLatestChangeForSymbol(symbol);
						// Convert the list of cells to a string for display
						String slc = Cell.cellCollectionRepresentation(lc);
						sb.append("<td " + clsAttribute + " title=\"" + protect(title) + "\">").append(protect(slc)).append("</td>").append(nl);					
					}
					
					sb.append("</tr>").append(nl);
				}
			}
			sb.append("</table>").append(nl);
	
		}
		
		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	// -----------------------------------------------------------------------------------------
	
	// Produce some final diagnostics at the completion of a run

	void produceFinalDiagnostics(Solver.SolutionStepStatus status, int finalStepNumber, long tookms, List<String> observations) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Final Status" + "</h2>").append(nl);			

		// Show any errors first
		if(observations.size() > 0)
		sb.append("<ul class=observation>").append(nl);
		for(String obs : observations) {
			sb.append("<li>").append(protect(obs)).append("</li>").append(nl);
		}
		sb.append("</ul>").append(nl);
		
		if(status.m_isComplete) {
			sb.append("The puzzle was <b>completed</b>, taking <b>" + tookms + " ms</b> and <b>" + finalStepNumber + " steps</b>.").append(nl);
		}
		else {
			sb.append("<b>Failed to complete</b> the puzzle, after " + tookms + " ms and " + finalStepNumber + " steps.").append(nl);
		}
		
		sb.append("<p/>").append(nl);
		
		// Check whether the final grid (complete or not) has self-consistent assignments
		List<Cell> badCells = m_grid.listOfIncompatibleCells();
		if(badCells.size() > 0) {
			String badCellString = "";
			for(Cell cell : badCells) {
				badCellString += (cell.gridLocation() + " ");
			}
			
			sb.append("The final grid is <b>invalid</b> : see cells " + protect(badCellString)).append(nl);
		}
		else {
			sb.append("The final grid is <b>valid.</b>").append(nl);			
		}

		sb.append("<p/>").append(nl);
		
		// Show the final grid.
		GridFormatter formatter = new GridFormatter(m_grid);		
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay(), -1));

		sb.append("<p/>");
		
		// Show cell assignment stats.
		sb.append("<b>Cell assignments:</b>").append(nl);
		sb.append("<p/>");
		
		int givens = status.m_gridStats.m_initialAssignedCellCount;
		int deduced = status.m_gridStats.m_assignedCellCount - status.m_gridStats.m_initialAssignedCellCount;
		int unassigned = status.m_gridStats.m_unassignedCellCount;
		sb.append("<table class=basictable>").append(nl);
		sb.append("<tr>").append("<td width=150  class=basictable>").append("Given cells").append("</td>");
		sb.append("<td width=50 class=\"basictable given\">").append(givens).append("</td>").append("</tr>").append(nl);
		sb.append("<tr>").append("<td class=basictable>").append("Deduced cells").append("</td>");
		sb.append("<td class=\"basictable solved\">").append(deduced).append("</td>").append("</tr>").append(nl);
		sb.append("<tr>").append("<td class=basictable>").append("Unassigned cells").append("</td>");
		sb.append("<td class=basictable>").append(unassigned).append("</td>").append("</tr>").append(nl);
		sb.append("</table>").append(nl);
		
		// Show how often the different methods for making progress were used.
		sb.append("<p/>");
		sb.append("<b>Method usage:</b>").append(nl);
		sb.append("<p/>");
		
		sb.append("<table class=basictable>").append(nl);
		sb.append("<tr><th class=basictable align=left width=100>Method</th>");
		sb.append("<th class=basictable align=left width=550>Description</th>");
		sb.append("<th class=basictable align=right width=70>Tried</th>");
		sb.append("<th align=right width=70>Useful</th>");
		sb.append("<th class=basictable align=right width=100>Example</th></tr>").append(nl);
		for(Method m : m_solver.methods()) {
			sb.append("<tr>");
			sb.append("<td class=basictable>").append(protect(m.name())).append("</td>");
			sb.append("<td class=basictable>").append(protect(m.approachSummary())).append("</td>");
			sb.append("<td class=basictable align=right>").append(m.m_calledCount).append("</td>");
			sb.append("<td class=basictable align=right>").append(m.m_usefulCount).append("</td>");
			String firstUsefulStep = (m.m_firstUsefulStepNumber==-1) ? "" : "Step " + m.m_firstUsefulStepNumber; 
			sb.append("<td class=basictable align=right>").append(firstUsefulStep).append("</td>");
			sb.append("</tr>").append(nl);
		}
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}

	// -----------------------------------------------------------------------------------------
	
	/**
	 * Class to show how many symbols are still in contention for being assigned to a cell.  
	 */
	class CouldBeValueCountDisplay implements CellDiagnosticsProvider {
		
		public boolean hasStaticContent() {
			return false;
		}

		public String basicCellClass() {
			return "";
		}
		
		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = m_solver.assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
		
		public String cellDiagnostics(Cell cell) {
			CellAssessment ca = m_solver.assessmentForCell(cell);
			String representation = "" + ca.couldBeCount();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;			// '*' indicates ready to be assigned.
			}
			return representation;
		}
		
	}

	// -----------------------------------------------------------------------------------------

	/**
	 * Class to show which symbols are still in contention for being assigned to a cell.  
	 */
	class CouldBeValueDisplay implements CellDiagnosticsProvider {
		
		public boolean hasStaticContent() {
			return false;
		}

		// Use a dedicated CSS style as these table elements need to be wider than normal.
		public String basicCellClass() {
			return "couldbevaluecell";
		}
		
		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = m_solver.assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
		
		public String cellDiagnostics(Cell cell) {
			CellAssessment ca = m_solver.assessmentForCell(cell);
			String representation = "" + ca.couldBeSymbolsRepresentation();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;			// '*' indicates ready to be assigned.
			}
			return representation;
		}		
	}
}
