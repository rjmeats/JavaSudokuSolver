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

import diagnostics.*;

class SolverDiagnostics {

	private Solver m_solver;
	private Grid m_grid;
	private Symbols m_symbols;
	private String m_puzzleSource;
	
	private boolean m_produceHtmlDiagnostics;
	private int m_diagnosticsFrequency;
	private String m_htmlDiagnostics;	

	SolverDiagnostics(Solver solver, Grid grid, Symbols symbols, String puzzleSource) {
		m_solver = solver;
		m_grid = grid;
		m_symbols = symbols;
		m_puzzleSource = puzzleSource;
		
		m_produceHtmlDiagnostics = true;
		m_diagnosticsFrequency = 1;
		m_htmlDiagnostics = "";
		
		if(m_grid.cells().size() > 100) {
			m_diagnosticsFrequency = 10;
		}
		
		showGivenGrid();
	}

	String getHtmlDiagnostics() {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(nl);
		sb.append("<head>").append(nl);
		sb.append(getDiagnosticStyles()).append(nl);
		sb.append("</head>").append(nl);
		sb.append("<body>").append(nl);
		sb.append(m_htmlDiagnostics);
		sb.append("</body>").append(nl);
		sb.append("</html>").append(nl);

		return sb.toString();
	}
	
	private String getDiagnosticStyles() {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		
		sb.append("<style>").append(nl);
		sb.append(GridFormatter.getCSSStyles());
		sb.append(".couldbevaluecountcell {").append(nl);
		sb.append("    width: 20px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    text-align: center;").append(nl);
		sb.append("}").append(nl);
		sb.append(".couldbevaluecell {").append(nl);
		sb.append("    width: 100px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    text-align: center;").append(nl);
		sb.append("}").append(nl);
//		sb.append(".changed {").append(nl);
//		sb.append("    background-color: yellow;").append(nl);
//		sb.append("}").append(nl);
//		sb.append(".given {").append(nl);
//		sb.append("    background-color: lightsteelblue;").append(nl);
//		sb.append("}").append(nl);
//		sb.append(".solved {").append(nl);
//		sb.append("    background-color: aquamarine;").append(nl);
//		sb.append("}").append(nl);
		sb.append(".cellsettable {").append(nl);
		sb.append("    border: 6px solid gray;").append(nl);
		sb.append("    border-collapse: collapse;").append(nl);
		sb.append("}").append(nl);
		sb.append(".cellsettablerowtitle {").append(nl);
		sb.append("    font-weight: bold;").append(nl);
		sb.append("    width: 80px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    border-left: 1px solid grey;").append(nl);
		sb.append("    border-right: 2px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("}").append(nl);		
		sb.append(".cellsetcell {").append(nl);
		sb.append("    border-left: 1px solid grey;").append(nl);
//		sb.append("    border-right: 1px solid grey;").append(nl);
		sb.append("    border-top: 1px solid grey;").append(nl);
		sb.append("    border-bottom: 1px solid grey;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("}").append(nl);		
		sb.append(".observation { color: red; }").append(nl);
		sb.append("</style>").append(nl);
		
		return sb.toString();
	}

	void showGivenGrid() {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		
		sb.append("<table>").append(nl);

		sb.append("<tr>").append(nl);
		sb.append("<td width=200>Sudoku source:</td><td>").append(m_puzzleSource).append("</td>").append(nl);
		sb.append("</tr>").append(nl);

		sb.append("<tr>").append(nl);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
		sb.append("<td>Run at:</td><td>").append(dateFormat.format(m_solver.startTime())).append("</td>").append(nl);
		sb.append("</tr>").append(nl);

		sb.append("<tr>").append(nl);
		sb.append("<td>Shading key:</td><td>").append(nl);
		
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		sb.append("<td class=given>Given</td>").append(nl);
		sb.append("<td class=changed>Changed</td>").append(nl);
		sb.append("<td class=solved>Solved</td>").append(nl);
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);
		
		sb.append("</td></tr>").append(nl);
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		sb.append("<h2>Starting Grid" + "</h2>").append(nl);			

		GridFormatter formatter = new GridFormatter(m_grid);		
		
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		sb.append("<td>Given values<P>").append(nl);

		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay(), -1));

		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Cell numbering<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.CellNumberDisplayer(), -1));

		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Cell locations<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.CellLocationDisplayer(), -1));
		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Box numbering<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.BoxNumberDisplayer(), -1));
		sb.append("</td>").append(nl);
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	void collectDiagnosticsAfterStep(int stepNumber, boolean changedState, List<String> actions, List<String> stepObservations, boolean forceDiagnostics) {
		if(!m_produceHtmlDiagnostics) return;		
		
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		if(stepNumber == -1) {
			sb.append("<h2>Initial Grid Assessment" + "</h2>").append(nl);			
		}
		else {
			sb.append("<h2>Grid Assessment after Step " + stepNumber + "</h2>").append(nl);
		}
		
		sb.append("<p/>");
		for(String o : stepObservations) {
			sb.append("<div class=observation>" + o + "</div>").append("<p/>").append(nl);
		}
		
		sb.append("<ul>");
		for(String a : actions) {
			sb.append("<li>").append(a).append("</li>").append(nl);
		}
		sb.append("</ul>");

		if(!changedState) {
			sb.append("No state changes caused by this step").append("<p/>");
		}		
		else if(stepNumber % m_diagnosticsFrequency != 0 && stepNumber != -1 && (!forceDiagnostics)) {
			// Stop output volume becoming excessive for larger grid sizes 
		}
		else {
			GridFormatter formatter = new GridFormatter(m_grid);
			
			sb.append("<table>").append(nl);
			sb.append("<tr>").append(nl);
			sb.append("<td>Assigned values<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
			sb.append("<td>Could-be values count<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new SolverDiagnostics.CouldBeValueCountDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
			sb.append("<td>Could-be values list<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new SolverDiagnostics.CouldBeValueDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			sb.append("</tr>").append(nl);
			sb.append("</table>").append(nl);
			
			sb.append("<p>").append(nl);
			sb.append("Possible cells for each symbol in each row/column/box");
			sb.append("<p/>").append(nl);
	
			Set<Symbol> symbols = m_symbols.getSymbolSet();
			sb.append("<table class=cellsettable>").append(nl);
			sb.append("<tr>").append(nl);
			sb.append("<th class=\"cellsetcell cellsettablerowtitle\">").append("").append("</th>").append(nl);
			int colSpan = symbols.size();
			sb.append("<th class=\"cellsetcell cellsettablerowtitle\" colspan=" + colSpan + ">").append("Symbol").append("</th>").append(nl);
			sb.append("</tr>").append(nl);
			sb.append("<tr>").append(nl);
			sb.append("<th class=\"cellsetcell cellsettablerowtitle\">").append("").append("</th>").append(nl);
			for(Symbol symbol : symbols) {
				sb.append("<th class=\"cellsetcell\">").append(symbol.getRepresentation()).append("</th>").append(nl);			
			}
			sb.append("</tr>").append(nl);
			for(int cellSet = 1; cellSet <= 3; cellSet++) {
				for(CellSetAssessment cellset : m_solver.cellSetAssessments()) {
					if((cellSet == 1) && !(cellset instanceof RowAssessment)) continue;
					if((cellSet == 2) && !(cellset instanceof ColumnAssessment)) continue;
					if((cellSet == 3) && !(cellset instanceof BoxAssessment)) continue;
					String cls = "cellsetcell cellsettablerowtitle";
					if(cellset.stepNumberOfLatestChange() == stepNumber) {
						cls += " changed";
					}
					
					cls = "\"" + cls + "\"";
					sb.append("<tr>").append(nl);
					sb.append("<td  class=" + cls + ">").append(cellset.getRepresentation()).append("</td>").append(nl);					
					for(Symbol symbol : symbols) {
						cls = "cellsetcell";
						List<Cell> lc = new ArrayList<>(cellset.couldBeCellsForSymbol(symbol));
						String slc = Cell.cellCollectionRepresentation(lc);
						boolean changed = (cellset.stepNumberOfLatestChangeForSymbol(symbol) == stepNumber);
						if(changed) {
							cls += " changed";
						}
						if(lc.size() == 1) {
							Cell cell = lc.get(0);
							boolean given = cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given;
							if(changed) {
							}
							else if(given) {
								cls += " given";					
							}
							else if(cell.isAssigned()) {
								cls += " solved";
							}
						}
						cls = "\"" + cls + "\"";
						String title = "lastchange=" + cellset.stepNumberOfLatestChangeForSymbol(symbol);
						sb.append("<td  class=" + cls + " title=\"" + title + "\">").append(slc).append("</td>").append(nl);					
					}
					
					sb.append("</tr>").append(nl);
				}
			}
			sb.append("</table>").append(nl);
	
			sb.append("</tr>").append(nl);
			sb.append("</table>").append(nl);

		}
		
		sb.append("<p/><hr/><p/>").append(nl);

//		if(stepNumber < 3)
		m_htmlDiagnostics += sb.toString();
	}
	
	public void finaliseDiagnostics(int steps, long ms, List<String> observations) {
//		if(!m_produceHtmlDiagnostics) return;
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Final Grid" + "</h2>").append(nl);			

		if(observations.size() > 0)
		sb.append("<ul class=observation>").append(nl);
		for(String o : observations) {
			sb.append("<li>").append(o).append("</li>").append(nl);
		}
		sb.append("</ul>").append(nl);
		
		sb.append("<p/>Took " + ms + " ms, " + steps + " steps<p/>").append(nl);
		
		GridFormatter formatter = new GridFormatter(m_grid);		
		sb.append(formatter.formatGridAsHTML(new GridDiagnostics.AssignedValueDisplay(), -1));

		sb.append("<p/>");
		
		sb.append("<table>").append(nl);
		sb.append("<tr><th align=left width=100>Method</th><th align=left width=500>Approach</th><th align=right width=100>");
		sb.append("Tried</th><th align=right width=100>Useful</th><th align=right width=200>First useful step</th></tr>");
		for(Method m : m_solver.methods()) {
			sb.append("<tr>");
			sb.append("<td>").append(m.getName()).append("</td>");
			sb.append("<td>").append(m.getApproachSummary()).append("</td>");
			sb.append("<td align=right>").append(m.m_calledCount).append("</td>");
			sb.append("<td align=right>").append(m.m_usefulCount).append("</td>");
			String firstUsefulStep = (m.m_firstUsefulStepNumber==-1) ? "" : m.m_firstUsefulStepNumber + ""; 
			sb.append("<td align=right>").append(firstUsefulStep).append("</td>");
			sb.append("</tr>").append(nl);
		}
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}

	class CouldBeValueCountDisplay implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			CellAssessment ca = m_solver.assessmentForCell(cell);
			String representation = "" + ca.couldBeCount();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			return(GridDiagnostics.padRight(representation, 5));
		}
		
		public String getBasicCellClass() {
			return "couldbevaluecountcell";
		}
		
		public boolean hasStaticContent() {
			return false;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = m_solver.assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
	}

	class CouldBeValueDisplay implements CellDiagnosticsProvider {
		
		public String getCellDiagnostics(Cell cell) {
			CellAssessment ca = m_solver.assessmentForCell(cell);
			String representation = "" + ca.couldBeSymbolsRepresentation();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			return(GridDiagnostics.padRight(representation, 17));
		}
		
		public String getBasicCellClass() {
			return "couldbevaluecell";
		}
		
		public boolean hasStaticContent() {
			return false;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = m_solver.assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
	}
}
