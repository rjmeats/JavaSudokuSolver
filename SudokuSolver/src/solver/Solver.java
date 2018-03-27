package solver;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import grid.Cell;
import grid.CellSet;
import grid.Row;
import grid.Symbol;
import grid.Symbols;
import grid.Column;
import grid.Assignment;
import grid.AssignmentMethod;
import grid.Box;
import grid.Grid;
import diagnostics.FormatUtils;
import diagnostics.GridFormatter;

public class Solver {

	Grid m_grid;	
	Symbols m_symbols;
	
	private List<CellAssessment> m_lCellAssessments;
	private List<CellSetAssessment> m_lCellSetAssessments;
	
	private HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	private HashMap<CellSet, CellSetAssessment> m_cellSetAssessmentsMap;

	private boolean m_produceHtmlDiagnostics;
	private int m_diagnosticsFrequency;
	private String m_htmlDiagnostics;	
	List<String> m_observations;
	List<String> m_stepObservations;

	Method1 m_method1;
	Method2 m_method2;
	Method3 m_method3;
	Method4 m_method4;
	
	public Solver(Grid grid, Symbols symbols) {
		m_grid = grid;
		m_symbols = symbols;

		m_lCellAssessments = new ArrayList<>();
		m_lCellSetAssessments = new ArrayList<>();

		m_cellAssessmentsMap = new HashMap<>();
		m_cellSetAssessmentsMap = new HashMap<>();
		
		for(Row row : m_grid.rows()) {
			RowAssessment rowAssessment = new RowAssessment(row, symbols);
			m_lCellSetAssessments.add(rowAssessment);
			m_cellSetAssessmentsMap.put(row, rowAssessment);
		}
		
		for(Column column : m_grid.columns()) {
			ColumnAssessment columnAssessment = new ColumnAssessment(column, symbols);
			m_lCellSetAssessments.add(columnAssessment);
			m_cellSetAssessmentsMap.put(column, columnAssessment);
		}

		for(Box box: m_grid.boxes()) {
			BoxAssessment boxAssessment = new BoxAssessment(box, symbols);
			m_lCellSetAssessments.add(boxAssessment);
			m_cellSetAssessmentsMap.put(box, boxAssessment);
		}

		for(Cell cell : m_grid.cells()) {
			setUpCellAssessment(cell);
		}		

		// Need to go through already-assigned given cells and do equivalent of Solver.applyGivenValueToCell processing to these cells to track initial state.
		for(CellAssessment ca : m_lCellAssessments) {
			Cell cell = ca.cell();
			if(cell.isAssigned()) {
				Assignment assignment = cell.assignment();
				spreadAssignmentImpact(ca, assignment);
			}
		}
		
		m_produceHtmlDiagnostics = true;
		m_diagnosticsFrequency = 1;
		m_htmlDiagnostics = "";
		
		if(m_grid.cells().size() > 100) {
			m_diagnosticsFrequency = 10;
		}
		
		m_observations = new ArrayList<>();
		m_stepObservations = new ArrayList<>();

		m_method1 = new Method1(this, m_symbols);
		m_method2 = new Method2(this, m_symbols);
		m_method3 = new Method3(this, m_symbols);
		m_method4 = new Method4(this, m_symbols);

		collectInitialDiagnostics();
	}

	private void setUpCellAssessment(Cell cell) {
		RowAssessment row = (RowAssessment)assessmentForCellSet(cell.row());
		ColumnAssessment column = (ColumnAssessment)assessmentForCellSet(cell.column());
		BoxAssessment box = (BoxAssessment)assessmentForCellSet(cell.box());

		CellAssessment cellAssessment = new CellAssessment(cell, row, column, box, m_symbols);				
		m_lCellAssessments.add(cellAssessment);
		m_cellAssessmentsMap.put(cell, cellAssessment);
	}
	
	CellAssessment assessmentForCell(Cell cell) {
		return m_cellAssessmentsMap.get(cell);
	}
	
	CellSetAssessment assessmentForCellSet(CellSet cellSet) {
		return m_cellSetAssessmentsMap.get(cellSet);
	}
	
	public String getHtmlDiagnostics() { return m_htmlDiagnostics; }
	
	public boolean nextStep(int stepNumber) {

		boolean changedState = false;
		List<String> actions = new ArrayList<>();
		m_stepObservations = new ArrayList<>();
		
		if(!changedState) {
			changedState = m_method1.tryMethod(m_lCellSetAssessments, stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = m_method2.tryMethod(m_lCellAssessments, stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = m_method3.tryMethod(m_lCellSetAssessments, stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = m_method4.tryMethod(m_lCellSetAssessments, stepNumber, actions);
		}
		
		collectDiagnosticsAfterStep(stepNumber, actions);		
		m_observations.addAll(m_stepObservations);
		return changedState;
	}

	// Check that the assignment we're about to make is valid
	CellAssignmentStatus checkCellCanBeAssigned(CellAssessment ca, Assignment assignment) {
		Symbol symbol = assignment.symbol();
		CellAssignmentStatus status = null;
		
		if(ca.isAssigned()) {
			status = CellAssignmentStatus.CellAlreadyAssigned;
		}
		else if(!ca.couldBe(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOutForCell;			
		}
		else if(ca.isRuledOut(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOutForCell;			
		}
		else if(ca.rowAssessment().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInRow;
		}
		else if(ca.columnAssessment().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInColumn;			
		}
		else if(ca.boxAssessment().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInBox;			
		}
		else {
			status = CellAssignmentStatus.AssignmentAllowed;
		}
			

		return status;
	}

	// Make an assignment of a symbol to a cell, as long as there are no unexpected conflicts. After making the assignment, work out
	// what this means for related cells and cell-sets in terms of possibilities that are now ruled out.
	CellAssignmentStatus performAssignment(Assignment assignment) {
		CellAssessment ca = assessmentForCell(assignment.cell());
		CellAssignmentStatus status = checkCellCanBeAssigned(ca, assignment);
		if(status == CellAssignmentStatus.AssignmentAllowed) {
			assignment.cell().assign(assignment);
			spreadAssignmentImpact(ca, assignment);
			status = CellAssignmentStatus.AssignmentMade;
		}
		
		return status;
	}

	// Can be called for an initial 'given' assignment, or for one deduced
	void spreadAssignmentImpact(CellAssessment ca, Assignment assignment) {
		ca.assignmentMade(assignment.symbol(), assignment.stepNumber());		
		for(CellSetAssessment csa : ca.cellSetAssessments()) {
			spreadAssignmentImpact(csa, ca, assignment);			
		}
	}
	
	void spreadAssignmentImpact(CellSetAssessment csa, CellAssessment ca, Assignment assignment) {
		Cell assignmentCell = assignment.cell();

		// A cell in this cell-set has had an assignment made. Update the cell-set record to reflect this 
		csa.assignmentMade(assignment.symbol(), assignmentCell, assignment.stepNumber());
	
		// Go through the other cells in this cell-set, and rule out this symbol from being assigned to those cells 
		for(Cell otherCell : csa.cellSet().cells()) {
			if(otherCell != assignmentCell) {
				// This cell isn't assigned to the symbol
				spreadRulingOutImpact(otherCell, assignment.symbol(), assignment.stepNumber());
			}
		}		
	}	

	int spreadRulingOutImpact(Cell cell, Symbol symbol, int stepNumber) {
		int changeCount = 0;
		// This cell isn't assigned to the symbol
		CellAssessment otherCellInCellSet = assessmentForCell(cell);
		changeCount += otherCellInCellSet.ruleOutSymbol(symbol, stepNumber);
		// And update all the cell sets in which this other cell resides to reflect that the symbol is not in this cell
		// NB One of these == csa, but it should be harmless to repeat the ruling out
		for(CellSetAssessment csaOfOtherCell : otherCellInCellSet.cellSetAssessments()) {
			changeCount += csaOfOtherCell.ruleOutCellForSymbol(cell, symbol, stepNumber);
		}				
		
		return changeCount;
	}
	
	void reportAssignmentFailure(Assignment a, CellAssignmentStatus status) {
		String o = "Unexpected assignment failure at step " + a.stepNumber() + " : " + status.name() + " : " + a.toString();
		m_stepObservations.add(o);
		System.err.println(o);		
	}
	
	// --------------------------------------------------------------------------------
	
	
	// ==============================================================
	
	public class CouldBeValueCountDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(Cell cell) {
			CellAssessment ca = assessmentForCell(cell);
			String representation = "" + ca.couldBeCount();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			return(FormatUtils.padRight(representation, 5));
		}
		
		public String getBasicCellClass() {
			return "couldbevaluecountcell";
		}
		
		public boolean staticContent() {
			return false;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
	}
	
	public class CouldBeValueDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be' values"; }
		
		public String getContent(Cell cell) {
			CellAssessment ca = assessmentForCell(cell);
			String representation = "" + ca.couldBeSymbolsRepresentation();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			return(FormatUtils.padRight(representation, 17));
		}
		
		public String getBasicCellClass() {
			return "couldbevaluecell";
		}
		
		public boolean staticContent() {
			return false;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { 
			CellAssessment ca = assessmentForCell(cell);
			return (ca.stepNumberOfLatestChange() == stepNumber);
		}
	}

	// Part of Grid package ????
	public static class CellNumberDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Cell numbering"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.getNumberOnlyRepresentation(), 5));
		}
		public String getBasicCellClass() {
			return "gridcell";
		}
		
		public boolean staticContent() {
			return true;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class CellLocationDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Cell locations"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.getGridLocationString(), 5));
		}
		public String getBasicCellClass() {
			return "gridcell";
		}
		
		public boolean staticContent() {
			return true;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class BoxNumberDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.box().getNumberOnlyRepresentation(), 5));
		}

		public String getBasicCellClass() {
			return "gridcell";
		}
		
		public boolean staticContent() {
			return true;
		}

		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}
	
	public static class AssignedValueDisplay implements CellContentProvider {
		
		public String getHeading() { return "Assigned-value"; }
		
		public String getContent(Cell cell) {
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

		public boolean staticContent() {
			return false;
		}

		public String getBasicCellClass() {
			return "gridcell";
		}
	}
	
	void collectInitialDiagnostics() {
//		if(!m_produceHtmlDiagnostics) return;
		m_htmlDiagnostics = "";
		showGivenGrid();
		collectDiagnosticsAfterStep(-1, new ArrayList<String>());
	}

	void showGivenGrid() {
//		if(!m_produceHtmlDiagnostics) return;
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		
		sb.append("<h2>Shading key" + "</h2>").append(nl);			
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		sb.append("<td class=given>Given</td>").append(nl);
		sb.append("<td class=highlight>Just changed</td>").append(nl);
		sb.append("<td class=previouslyassigned>Assigned</td>").append(nl);
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);
		
		sb.append("<p/><hr/><p/>").append(nl);

		sb.append("<h2>Starting Grid" + "</h2>").append(nl);			

		GridFormatter formatter = new GridFormatter(m_grid);		
		
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		sb.append("<td>Given values<P>").append(nl);

		sb.append(formatter.formatGridAsHTML(new Solver.AssignedValueDisplay(), -1));

		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Cell numbering<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new Solver.CellNumberDisplayer(), -1));

		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Cell locations<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new Solver.CellLocationDisplayer(), -1));
		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>Box numbering<p/>").append(nl);

		sb.append(formatter.formatGridAsHTML(new Solver.BoxNumberDisplayer(), -1));
		sb.append("</td>").append(nl);
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	public void showFinalGrid() {
//		if(!m_produceHtmlDiagnostics) return;
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Final Grid" + "</h2>").append(nl);			

		GridFormatter formatter = new GridFormatter(m_grid);		
		sb.append(formatter.formatGridAsHTML(new Solver.AssignedValueDisplay(), -1));

		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	void collectDiagnosticsAfterStep(int stepNumber, List<String> actions) {
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
		for(String o : m_stepObservations) {
			sb.append("<div class=observation>" + o + "</div>").append("<p/>").append(nl);
		}
		
		sb.append("<ul>");
		for(String a : actions) {
			sb.append("<li>").append(a).append("</li>").append(nl);
		}
		sb.append("</ul>");
		
		if(stepNumber % m_diagnosticsFrequency != 0 && stepNumber != -1) {
		}
		else {
			GridFormatter formatter = new GridFormatter(m_grid);
			
			sb.append("<table>").append(nl);
			sb.append("<tr>").append(nl);
			sb.append("<td>Assigned values<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new Solver.AssignedValueDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
			sb.append("<td>Could-be values count<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new Solver.CouldBeValueCountDisplay(), stepNumber));
			sb.append("</td>").append(nl);
			sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
			sb.append("<td>Could-be values list<p/>").append(nl);
			sb.append(formatter.formatGridAsHTML(new Solver.CouldBeValueDisplay(), stepNumber));
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
				for(CellSetAssessment cellset : m_lCellSetAssessments) {
					if((cellSet == 1) && !(cellset instanceof RowAssessment)) continue;
					if((cellSet == 2) && !(cellset instanceof ColumnAssessment)) continue;
					if((cellSet == 3) && !(cellset instanceof BoxAssessment)) continue;
					String cls = "cellsetcell cellsettablerowtitle";
					if(cellset.stepNumberOfLatestChange() == stepNumber) {
						cls += " highlight";
					}
					
					cls = "\"" + cls + "\"";
					sb.append("<tr>").append(nl);
					sb.append("<td  class=" + cls + ">").append(cellset.getRepresentation()).append("</td>").append(nl);					
					for(Symbol symbol : symbols) {
						cls = "cellsetcell";
						List<Cell> lc = new ArrayList<>(cellset.getCouldBeCellsForSymbol(symbol));
						String slc = Cell.cellCollectionRepresentation(lc);
						boolean highlight = false; // provider.changedThisStep(cell,  stepNumberToHighlight);
						highlight = (cellset.stepNumberOfLatestChangeForSymbol(symbol) == stepNumber);
						if(highlight) {
							cls += " highlight";
						}
						if(lc.size() == 1) {
							Cell cell = lc.get(0);
							//CellAssessment ca = ;
							//highlight = (assessmentForCell(cell).stepNumberOfLatestChange() == stepNumber);
							boolean given = cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given;
							if(highlight) {
	//							cls += " highlight";
							}
							else if(given) {
								cls += " given";					
							}
							else if(cell.isAssigned()) {
								cls += " previouslyassigned";
							}
	
							//if(cell.assignment().method() == )
							//cls += " previouslyassigned";		// Handle Given, and assigned-during-this-step
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
	
	public void finaliseDiagnostics(int steps, long ms) {
//		if(!m_produceHtmlDiagnostics) return;
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Summary</h2>").append(nl);			

		if(m_observations.size() > 0)
		sb.append("<ul class=observation>").append(nl);
		for(String o : m_observations) {
			sb.append("<li>").append(o).append("</li>").append(nl);
		}
		sb.append("</ul>").append(nl);
		
		sb.append("<p/>Took " + ms + " ms, " + steps + " steps<p/>").append(nl);
System.out.println("Took " + ms + " ms.");		
		sb.append("<p/><hr/><p/>").append(nl);
		// ???? Stats ????
		// Show initial and final grids, whether complete, whether invalid ????
		m_htmlDiagnostics += sb.toString();
	}
}

enum CellAssignmentStatus {
	AssignmentAllowed, AssignmentMade, CellAlreadyAssigned, SymbolAlreadyRuledOutForCell, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}
