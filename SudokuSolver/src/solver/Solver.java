package solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;

import grid.Cell;
import grid.CellSet;
import grid.Row;
import grid.Column;
import grid.Box;
import grid.Grid9x9;

import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Symbol;
import puzzle.SymbolsToUse;
import diagnostics.FormatUtils;
import diagnostics.GridFormatter;

public class Solver {

	Grid9x9 m_grid;	
	SymbolsToUse m_symbols;
	
	private List<CellAssessment> m_lCellAssessments;
	private List<CellSetAssessment> m_lCellSetAssessments;
	
	private HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	private HashMap<CellSet, CellSetAssessment> m_cellSetAssessmentsMap;

	private String m_htmlDiagnostics;	
	List<String> m_observations;
	List<String> m_stepObservations;

	Method1 m_method1;
	Method2 m_method2;
	Method3 m_method3;
	Method4 m_method4;
	
	public Solver(Grid9x9 grid, SymbolsToUse symbols) {
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
		
		m_htmlDiagnostics = "";
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
		row.addCell(cell);
		ColumnAssessment column = (ColumnAssessment)assessmentForCellSet(cell.column());
		column.addCell(cell);
		BoxAssessment box = (BoxAssessment)assessmentForCellSet(cell.box());
		box.addCell(cell);		

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
		Symbol symbol = assignment.symbol();
		Cell assignmentCell = assignment.cell();

		// A cell in this cell-set has had an assignment made. Update the cell-set record to reflect this 
		csa.assignmentMade(assignment.symbol(), assignmentCell, assignment.stepNumber());
	
		// Go through the other cells in this cell-set, and rule out this symbol from being assigned to those cells 
		for(Cell otherCell : csa.getCellSet().getCells()) {
			if(otherCell != assignmentCell) {
				// This cell isn't assigned to the symbol
				CellAssessment otherCellInCellSet = assessmentForCell(otherCell);
				otherCellInCellSet.ruleOutSymbol(symbol, assignment.stepNumber());
				// And update all the cell sets in which this other cell resides to reflect that the symbol is not in this cell
				// NB One of these == csa, but it should be harmless to repeat the ruling out
				for(CellSetAssessment csaOfOtherCell : otherCellInCellSet.cellSetAssessments()) {
					csaOfOtherCell.ruleOutCellForSymbol(otherCell, symbol, assignment.stepNumber());
				}				
			}
		}		
	}	

	void reportAssignmentFailure(Assignment a, CellAssignmentStatus status) {
		String o = "Unexpected assignment failure at step " + a.stepNumber() + " : " + status.name() + " : " + a.toString();
		m_stepObservations.add(o);
		System.err.println(o);		
	}
	
	// --------------------------------------------------------------------------------
	
	
	private static String s_divider = "-----------------------------------";

	public void printCellSets() {
		printCellSets(-1);
	}
	
	public void printCellSets(int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append("Cell sets "  + stepInfo);
		sb1.append("\r\n");
		
		for(CellSetAssessment cellset : m_lCellSetAssessments) {
			sb1.append(cellset.getRepresentation() + " : " + cellset.getSymbolAssignmentSummary());
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}

	public void printGrid(CellContentProvider ccd) { printGrid(ccd, -1); }
	
	public void printGrid(CellContentProvider ccd, int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append(ccd.getHeading() + stepInfo);
		sb1.append("\r\n");

		GridFormatter gf = new GridFormatter(m_grid);
		sb1.append(gf.formatGrid(ccd, stepNumber));
		System.out.println(sb1.toString());
	}

	// ==============================================================
	
	public class CouldBeValueCountDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(Cell cell) {
			CellAssessment ca = assessmentForCell(cell);
			String representation = "" + ca.couldBeCount();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(cell.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 5));
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
			String representation = "" + ca.toCouldBeSymbolsString();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (cell.isAssigned() && cell.assignment().method() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(cell.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 17));
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
			return(FormatUtils.padRight(cell.cellNumber(), 5));
		}
		public boolean changedThisStep(Cell cell, int stepNumber) { return false; }
	}

	public static class BoxNumberDisplayer implements CellContentProvider {
		
		public String getHeading() { return "Box numbering"; }
		
		public String getContent(Cell cell) {
			return(FormatUtils.padRight(cell.box().getBoxNumber(), 5));
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

	}
	
	void collectInitialDiagnostics() {
		m_htmlDiagnostics = "";
		collectDiagnosticsAfterStep(-1, new ArrayList<String>());
	}

	void collectDiagnosticsAfterStep(int stepNumber, List<String> actions) {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		if(stepNumber < 1) {
			sb.append("<h2>Initial Grid" + "</h2>").append(nl);			
		}
		else {
			sb.append("<h2>Grid after Step " + stepNumber + "</h2>").append(nl);
		}
		
		sb.append("<p/>");
		for(String o : m_stepObservations) {
			sb.append("<div class=observation>" + o + "</div>").append("<p/>").append(nl);
		}
		
		sb.append("<p/>");
		for(String a : actions) {
			sb.append(a).append("<p/>").append(nl);
		}
		
		GridFormatter formatter = new GridFormatter(m_grid);
		
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		sb.append("<td>").append(nl);
		sb.append(formatter.formatGridAsHTML(new Solver.AssignedValueDisplay(), stepNumber));
		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>").append(nl);
		sb.append(formatter.formatGridAsHTML(new Solver.CouldBeValueCountDisplay(), stepNumber));
		sb.append("</td>").append(nl);
		sb.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;").append(nl);
		sb.append("<td>").append(nl);
		sb.append(formatter.formatGridAsHTML(new Solver.CouldBeValueDisplay(), stepNumber));
		sb.append("</td>").append(nl);
		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);
		
		sb.append("<p/>").append(nl);
		sb.append("<table>").append(nl);
		sb.append("<tr>").append(nl);
		
		for(int cellSet = 1; cellSet <= 3; cellSet++) {
			
//			sb.append("<td>").append(nl);
//			sb.append("<table>").append(nl);
			for(CellSetAssessment cellset : m_lCellSetAssessments) {
				if((cellSet == 1) && !(cellset instanceof RowAssessment)) continue;
				if((cellSet == 2) && !(cellset instanceof ColumnAssessment)) continue;
				if((cellSet == 3) && !(cellset instanceof BoxAssessment)) continue;
				sb.append("<tr>").append(nl);
				String bgColor = cellset.stepNumberOfLatestChange() == stepNumber ? " bgcolor=cyan" : "";
				sb.append("<td" + bgColor + ">").append(nl);
				sb.append(cellset.getOneBasedRepresentation() + " : " + cellset.getSymbolAssignmentSummary() + "<br/>");
				sb.append("</td>").append(nl);
				sb.append("</tr>").append(nl);
			}
//			sb.append("</table>").append(nl);
//			sb.append("</td>").append(nl);
		}
			

		sb.append("</tr>").append(nl);
		sb.append("</table>").append(nl);

		
		sb.append("<p/><hr/><p/>").append(nl);

		m_htmlDiagnostics += sb.toString();
	}
	
	public void finaliseDiagnostics() {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Summary</h2>").append(nl);			

		if(m_observations.size() > 0)
		sb.append("<ul class=observation>").append(nl);
		for(String o : m_observations) {
			sb.append("<li>").append(o).append("</li>").append(nl);
		}
		sb.append("</ul>").append(nl);
		
		// ???? Stats ????
		// Show initial and final grids, whether complete, whether invalid ????
		m_htmlDiagnostics += sb.toString();
	}
}

enum CellAssignmentStatus {
	AssignmentAllowed, AssignmentMade, CellAlreadyAssigned, SymbolAlreadyRuledOutForCell, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}

abstract class Method {
	
	Solver m_solver;
	SymbolsToUse m_symbols;
	
	Method(Solver solver, SymbolsToUse symbols) {
		m_solver = solver;
		m_symbols = symbols;
	}
}
	

class Method1 extends Method {
	
	Method1(Solver solver, SymbolsToUse symbols) {
		super(solver, symbols);
	}
	
	// Look through each cell-set (row, column, and box) in turn for an unassigned symbol which can now go in only one of the cells in the cell-set
	boolean tryMethod(List<CellSetAssessment> cellSetAssessments, int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellSetAssessment csa : cellSetAssessments) {
			Assignment a = hasSymbolAssignmentAvailable(csa, stepNumber);
			if(a != null) {
				CellAssignmentStatus status = m_solver.performAssignment(a);
				if(status == CellAssignmentStatus.AssignmentMade) {
					String s = "Assigned symbol " + a.symbol().getRepresentation() + " to Cell " + a.cell().getOneBasedGridLocationString() + " for " + csa.getOneBasedRepresentation();
					actions.add(s);
					changedState = true;
				}
				// We don't expect the assignment to fail, implies some sort of logic error. 
				else {
					m_solver.reportAssignmentFailure(a, status);
				}
				break;
			}
		}
		return changedState;
	}
	
	// Does a cell-set (box, row, column) have a symbol which is not yet assigned but can only be assigned to one of the cells ? If so, we can make an assignment.
	Assignment hasSymbolAssignmentAvailable(CellSetAssessment csa, int stepNumber) {
		Assignment assignment = null;
		for(Symbol symbol : m_symbols.getSymbolSet()) {
			if(!csa.symbolAlreadyAssigned(symbol)) {
				Cell onlyCell = csa.getOnlyCouldBeCellForSymbol(symbol);
				if(onlyCell != null) {
					String detail = "Only cell available for symbol" + csa.getRepresentation();
					assignment = new Assignment(onlyCell, symbol, AssignmentMethod.AutomatedDeduction, detail,  stepNumber);
					break;
				}
			}
		}
		
		return assignment;
	}
}

class Method2 extends Method {
	
	Method2(Solver solver, SymbolsToUse symbols) {
		super(solver, symbols);
	}
	
	// Look through unassigned cells for cases where only one symbol is now left as a possible assignment for the cell.
	boolean tryMethod(List<CellAssessment> lCellAssessments, int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellAssessment ca : lCellAssessments) {
			Assignment a = hasSymbolAssignmentAvailable(ca, stepNumber);
			if(a != null) {
				CellAssignmentStatus status = m_solver.performAssignment(a);
				if(status == CellAssignmentStatus.AssignmentMade) {
					String s = "Assigned only possible symbol " + a.symbol().getRepresentation() + " to Cell " + ca.cell().getOneBasedGridLocationString();
					actions.add(s);
					changedState = true;
				}
				else {
					m_solver.reportAssignmentFailure(a, status);
				}
				break;
			}
		}			
		return changedState;
	}

	// If there is only one symbol which can still be assigned to this cell, then we have an assignment 
	Assignment hasSymbolAssignmentAvailable(CellAssessment ca, int stepNumber) {
		Assignment assignment = null;
		if(!ca.isAssigned())
		{
			Symbol onlySymbol = ca.getOnlyCouldBeSymbolForCell();
			if(onlySymbol != null) {
				assignment = new Assignment(ca.cell(), onlySymbol, AssignmentMethod.AutomatedDeduction, "Only symbol available for cell", stepNumber);
			}
		}
		return assignment;
	}


}


class Method3 extends Method {
	
	Method3(Solver solver, SymbolsToUse symbols) {
		super(solver, symbols);
	}
	
	// Look through each box to see where a particular unresolved symbol can only appear in a specific row or column of the box.
	// Where this arises, we can rule-out the symbol from the other cells in the row or column which are not in the box.
	//
	// For example (x or * can be a known or unknown value)
	//
	//                                  C7  C8  C9	
	// x	x	x		x	x	x		3	1	.	R1
	// *	*	*		*	*	*		.	.	.	R2
	// x	7	x		x	x	x		.	.	5	R3
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	//
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	x
	// x	x	x		x	x	x		x	x	7
	//
	// For the top-right box (Box 3), the 7 can only be in Row 2. 
	// Consequently we can rule out 7 from being any of the cells in Row 2 which are outside Box 3
	// So the cells marked with a * cannot be a 7, and we can apply this 7-is-ruled-out restriction to those cells.	
	
	boolean tryMethod(List<CellSetAssessment> cellSetAssessments, int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellSetAssessment csa : cellSetAssessments) {
			List<Method3Restriction> lRestrictions = findMethod3Restrictions(csa);
			changedState = applyMethod3Restrictions(lRestrictions, stepNumber, actions);			
			if(changedState) break;
		}
		return changedState;
	}
	
	private static class Method3Restriction {
		Symbol m_symbol;
		CellSet m_restrictorCellSet;
		CellSet m_restrictedCellSet;
		Set<Cell> m_restrictedCells;
		
		Method3Restriction(Symbol symbol, CellSet restrictor, CellSet restricted) {
			m_symbol = symbol;
			m_restrictorCellSet = restrictor;
			m_restrictedCellSet = restricted;
			m_restrictedCells = m_restrictedCellSet.getCellsNotIn(m_restrictorCellSet);
		}
		
		String getRepresentation() {
			return "Symbol " + m_symbol.getRepresentation() + " in " + m_restrictorCellSet.getOneBasedRepresentation() + 
						" restricted to " + m_restrictedCellSet.getOneBasedRepresentation() + " : symbol cannot be present in cells: " + Cell.cellCollectionToString(m_restrictedCells);
		}
	}

	private List<Method3Restriction> findMethod3Restrictions(CellSetAssessment csa) {
		
		CellSet thisCellSet = csa.getCellSet();
		List<Method3Restriction> lRestrictions = new ArrayList<>();		
		
		for(Symbol symbol : csa.getSymbols()) {
			List<Cell> lCells = csa.getCouldBeCellsForSymbol(symbol);
			if(lCells.size() > 1) {
				Set<Box> boxSet = new HashSet<>();
				Set<Row> rowSet = new HashSet<>();
				Set<Column> columnSet = new HashSet<>();
				for(Cell cell : lCells) {
					boxSet.add(cell.box());
					rowSet.add(cell.row());
					columnSet.add(cell.column());
				}

				CellSet restrictedCellSet = null;
				if(boxSet.size() == 1) {
					Box box = lCells.get(0).box();
					if(box != thisCellSet) {
						restrictedCellSet = box;
					}
				}
				
				if(rowSet.size() == 1) {
					Row row = lCells.get(0).row();
					if(row != thisCellSet) {
						restrictedCellSet = row;
					}
				}
				
				if(columnSet.size() == 1) {
					Column column = lCells.get(0).column();
					if(column != thisCellSet) {
						restrictedCellSet = column;
					}
				}				

				if(restrictedCellSet != null) {
					lRestrictions.add(new Method3Restriction(symbol, thisCellSet, restrictedCellSet));
				}
			}
		}
		
		return lRestrictions;
	}	
		
	private boolean applyMethod3Restrictions(List<Method3Restriction> lRestrictions, int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(Method3Restriction restriction : lRestrictions) {
			int changeCount = 0;
			for(Cell cell : restriction.m_restrictedCells) {
				CellAssessment ca = m_solver.assessmentForCell(cell);
				changeCount += ca.ruleOutSymbol(restriction.m_symbol, stepNumber);
			}

			if(changeCount > 0) {
				changedState = true;
				actions.add(restriction.getRepresentation());
				break;
			}
		}

		return changedState;
	}
}

class Method4 extends Method {
	
	Method4(Solver solver, SymbolsToUse symbols) {
		super(solver, symbols);
	}
	
	// Where n symbols in a row/column/box can only be assigned to the same n cells, then these cells can't be assigned to any other symbols.
	boolean tryMethod(List<CellSetAssessment> cellSetAssessments, int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(CellSetAssessment set : cellSetAssessments) {
			List<SymbolSetRestriction> lRestrictedSymbolSets = findRestrictedSymbolSets(set);
			if(lRestrictedSymbolSets != null) {
				for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {						
					for(Cell cell : symbolSetRestriction.m_lCells) {
						CellAssessment ca = m_solver.assessmentForCell(cell);
						boolean causedStateChange = ca.ruleOutAllSymbolsExcept(symbolSetRestriction.m_lSymbols, stepNumber);
						if(causedStateChange) {
							stateChanges++;
							String s = "Restriction: " + symbolSetRestriction.getRepresentation();
							actions.add(s);
						}
					}
				}
	
				for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {
					for(Symbol symbol : symbolSetRestriction.m_lSymbols) {
						CellSetAssessment cseta = m_solver.assessmentForCellSet(symbolSetRestriction.m_cellSet);
						int causedChange = cseta.ruleOutAllOtherCellsForSymbol(symbolSetRestriction.m_lCells, symbol, stepNumber);
						if(causedChange > 0) {
							stateChanges++;
							String s = "Restriction: " + symbolSetRestriction.getRepresentation();
							actions.add(s);
						}
					}
					
					List<CellSet> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
					for(CellSet cset : lAffectedCellSets) {
						CellSetAssessment cseta = m_solver.assessmentForCellSet(cset);
						for(Cell cell : symbolSetRestriction.m_lCells) {
							int causedChange = cseta.ruleOutCellFromOtherSymbols(cell, symbolSetRestriction.m_lSymbols, stepNumber);
							if(causedChange > 0) {
								stateChanges++;
								String s = "Restriction: " + symbolSetRestriction.getRepresentation();
								actions.add(s);
							}
						}
					}
				}
			}
		}
		
		changedState = (stateChanges > 0);
		return changedState;
	}
	
	// Goes up to combinations of 4 - how to generalise to n ?
	List<SymbolSetRestriction> findRestrictedSymbolSets(CellSetAssessment csa) {
		List<SymbolSetRestriction> l = new ArrayList<>();
		// Generate combinations of 2, 3 and 4 unassigned symbols. If the combination has n symbols and between them these can only
		// be placed in n cells, then we have a restricted symbol set.
		
		List<List<Symbol>> lCombinations = new ArrayList<>();
		
		for(Symbol symbol1 : csa.getSymbols()) {
			List<Cell> lCells1 = csa.getCouldBeCellsForSymbol(symbol1);
			if(lCells1.size() > 1) {

				for(Symbol symbol2 : csa.getSymbols()) {
					if(symbol2.ordinal() > symbol1.ordinal()) {
						List<Cell> lCells2 = csa.getCouldBeCellsForSymbol(symbol2);
						if(lCells2.size() > 1) {
							// We have a combination of two symbols to investigate ...
							List<Symbol> l2 = new ArrayList<>();
							l2.add(symbol1); l2.add(symbol2);
							lCombinations.add(l2);
							
							for(Symbol symbol3 : csa.getSymbols()) {
								if(symbol3.ordinal() > symbol2.ordinal()) {
									List<Cell> lCells3 = csa.getCouldBeCellsForSymbol(symbol3);
									if(lCells3.size() > 1) {
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);

										for(Symbol symbol4 : csa.getSymbols()) {
											if(symbol4.ordinal() > symbol3.ordinal()) {
												List<Cell> lCells4 = csa.getCouldBeCellsForSymbol(symbol4);
												if(lCells4.size() > 1) {
													// We have a combination of four symbols to investigate ...
													List<Symbol> l4 = new ArrayList<>(l3); l4.add(symbol4); 
													lCombinations.add(l4);													
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}			
		}

		System.err.println("Found " + lCombinations.size() + " symbol combinations for " + csa.getCellSet().getRepresentation());
		for(List<Symbol> lCombination : lCombinations) {
			List<Cell> lCellsForCombination = getSymbolCombinationCells(csa, lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet) {
				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolCollectionToString(lCombination) + " covers cells " +  Cell.cellCollectionToString(lCellsForCombination));				
				SymbolSetRestriction restriction = new SymbolSetRestriction(csa.getCellSet(), lCombination, lCellsForCombination);
				l.add(restriction);
			}
		}		
		
		return l;
	}

	private List<Cell> getSymbolCombinationCells(CellSetAssessment csa, List<Symbol> lCombination) {
		Set<Cell> cells = new TreeSet<>();
		for(Symbol symbol : lCombination) {
			List<Cell> l = csa.getCouldBeCellsForSymbol(symbol);
			for(Cell cell : l) {
				cells.add(cell);
			}
		}
				
		return new ArrayList<Cell>(cells);
	}

	//Paired symbols in a cell set which can only exist in a subset of cells. The two lists will be the same length.  
	private class SymbolSetRestriction {
		CellSet m_cellSet;	
		List<Symbol> m_lSymbols;
		List<Cell> m_lCells;
		
		SymbolSetRestriction(CellSet cellSet, List<Symbol> lSymbols, List<Cell> lCells) {
			m_cellSet = cellSet;	
			m_lSymbols = lSymbols;
			m_lCells = lCells;		
		}
		
		List<CellSet> getAffectedCellSets() {
			Set<CellSet> set = new TreeSet<>();		// Tree set maintains sorting order, LinkedHashSet maintains insertion order ????	
			for(Cell cell : m_lCells) {
				set.add(cell.box());
				set.add(cell.row());
				set.add(cell.column());
			}
			return new ArrayList<CellSet>(set);		
		}
		
		String getRepresentation() {
			return "SymbolSetRestriction for " + m_cellSet.getOneBasedRepresentation() + " Symbols: " + Symbol.symbolCollectionToString(m_lSymbols) + ", Cells : " + Cell.cellCollectionToString(m_lCells); 
		}
	}
}	
