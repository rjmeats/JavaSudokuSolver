package solver;

import java.util.HashMap;

import java.util.List;
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
	
	private List<RowAssessment> m_lRows;
	private List<ColumnAssessment> m_lColumns;
	private List<BoxAssessment> m_lBoxes;	
	private List<CellAssessment> m_lCellAssessments;
	private List<CellSetAssessment> m_lCellSets;
	
	private HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	private HashMap<Row, RowAssessment> m_rowAssessmentsMap;
	private HashMap<Column, ColumnAssessment> m_columnAssessmentsMap;
	private HashMap<Box, BoxAssessment> m_boxAssessmentsMap;
	private HashMap<CellSet, CellSetAssessment> m_cellSetAssessmentsMap;

	private String m_htmlDiagnostics;	
	List<String> m_observations;
	List<String> m_stepObservations;
	
	public Solver(Grid9x9 grid, SymbolsToUse symbols) {
		m_grid = grid;
		m_symbols = symbols;

		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellAssessments = new ArrayList<>();
		m_cellAssessmentsMap = new HashMap<>();
		m_rowAssessmentsMap = new HashMap<>();
		m_columnAssessmentsMap = new HashMap<>();
		m_boxAssessmentsMap = new HashMap<>();
		
		for(Row row : m_grid.rows()) {
			RowAssessment assessment = new RowAssessment(row, symbols);
			m_lRows.add(assessment);
			m_rowAssessmentsMap.put(row, assessment);
		}
		
		for(Column column : m_grid.columns()) {
			ColumnAssessment assessment = new ColumnAssessment(column, symbols);
			m_lColumns.add(assessment);
			m_columnAssessmentsMap.put(column, assessment);
		}

		for(Box box: m_grid.boxes()) {
			BoxAssessment assessment = new BoxAssessment(box, symbols);
			m_lBoxes.add(assessment);
			m_boxAssessmentsMap.put(box, assessment);
		}

		m_lCellSets = new ArrayList<>(m_lRows);
		m_lCellSets.addAll(m_lColumns);
		m_lCellSets.addAll(m_lBoxes);
		
		m_cellSetAssessmentsMap = new HashMap<>(m_rowAssessmentsMap);
		m_cellSetAssessmentsMap.putAll(m_columnAssessmentsMap);
		m_cellSetAssessmentsMap.putAll(m_boxAssessmentsMap);
			
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
		
		collectInitialDiagnostics();
	}

	private void setUpCellAssessment(Cell cell) {
		RowAssessment row = getRowAssessmentForCell(cell);
		row.addCell(cell);
		ColumnAssessment column = getColumnAssessmentForCell(cell);
		column.addCell(cell);
		BoxAssessment box = getBoxAssessmentForCell(cell);
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
	
	BoxAssessment getBoxAssessmentForBox(Box box) {
		return (BoxAssessment)m_cellSetAssessmentsMap.get(box);
	}
	
	RowAssessment getRowAssessmentForCell(Cell cell) {
		return m_rowAssessmentsMap.get(cell.row());
	}

	ColumnAssessment getColumnAssessmentForCell(Cell cell) {
		return m_columnAssessmentsMap.get(cell.column());
	}

	BoxAssessment getBoxAssessmentForCell(Cell cell) {
		return m_boxAssessmentsMap.get(cell.box());
	}

	public String getHtmlDiagnostics() { return this.m_htmlDiagnostics; }
	
	public boolean nextStep(int stepNumber) {

		boolean changedState = false;
		List<String> actions = new ArrayList<>();
		m_stepObservations = new ArrayList<>();
		
		if(!changedState) {
			changedState = tryMethod1(stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = tryMethod2(stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = tryMethod3(stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = tryMethod4(stepNumber, actions);
		}
		
		if(!changedState) {
			changedState = tryMethod5(stepNumber, actions);
		}
		
		if(!changedState)
		{
			changedState = tryMethod6(stepNumber, actions);
		}
		
		collectDiagnosticsAfterStep(stepNumber, actions);		
		m_observations.addAll(m_stepObservations);
		return changedState;
	}

	// Look through each cell-set (row, column, and box) in turn for an unassigned symbol which can now go in only one of the cells in the cell-set
	private boolean tryMethod1(int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellSetAssessment csa : m_lCellSets) {
			Assignment a = hasSymbolAssignmentAvailable(csa, stepNumber);
			if(a != null) {
				CellAssignmentStatus status = performAssignment(a);
				if(status == CellAssignmentStatus.AssignmentMade) {
					String s = "Assigned symbol " + a.symbol().getRepresentation() + " to Cell " + a.cell().getOneBasedGridLocationString() + " for " + csa.getOneBasedRepresentation();
					actions.add(s);
					changedState = true;
				}
				// We don't expect the assignment to fail, implies some sort of logic error. 
				else {
					reportAssignmentFailure(a, status);
				}
				break;
			}
		}
		return changedState;
	}
	
	// Look through unassigned cells for cases where only one symbol is now left as a possible assignment for the cell.
	private boolean tryMethod2(int stepNumber, List<String> actions) {
		boolean changedState = false;
		for(CellAssessment ca : m_lCellAssessments) {
			Assignment a = hasSymbolAssignmentAvailable(ca, stepNumber);
			if(a != null) {
				CellAssignmentStatus status = performAssignment(a);
				if(status == CellAssignmentStatus.AssignmentMade) {
					String s = "Assigned only possible symbol " + a.symbol().getRepresentation() + " to Cell " + ca.cell().getOneBasedGridLocationString();
					actions.add(s);
					changedState = true;
				}
				else {
					reportAssignmentFailure(a, status);
				}
				break;
			}
		}			
		return changedState;
	}

	private void reportAssignmentFailure(Assignment a, CellAssignmentStatus status) {
		String o = "Unexpected assignment failure at step " + a.stepNumber() + " : " + status.name() + " : " + a.toString();
		m_stepObservations.add(o);
		System.err.println(o);		
	}
	
	// Look through each box to see where a particular unresolved symbol can only appear in a specific row or column of the box.
	// Where this arises, we can rule-out the symbol from the other cells in the row or column which are not in the box.
	private boolean tryMethod3(int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(BoxAssessment box : m_lBoxes) {
			List<SymbolRestriction> lRestrictions = box.findRestrictedSymbols();
			if(lRestrictions != null) {
				for(SymbolRestriction restriction : lRestrictions) {
					CellSetAssessment csa = assessmentForCellSet(restriction.m_rowOrColumn);
					boolean causedStateChange = ruleOutSymbolOutsideBox(csa, restriction, stepNumber);
					if(causedStateChange) {
						String s = "Box restriction for " + restriction.getRepresentation();
						actions.add(s);
						stateChanges++;
						break;
					}
				}
			}
			
			if(stateChanges > 0) break;
		}
		changedState = (stateChanges > 0);
		return changedState;
	}
	
	// Look through each column to see where a particular unresolved symbol can only appear in a specific box.
	// Where this arises, we can rule-out the symbol from the other cells in the box which are not in the column.
	private boolean tryMethod4(int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(ColumnAssessment column : m_lColumns) {
			List<SymbolRestriction> lRestrictions = column.findRestrictedSymbols();
			if(lRestrictions != null) {
				for(SymbolRestriction restriction : lRestrictions) {
					BoxAssessment boxa = getBoxAssessmentForBox(restriction.m_box);
					boolean causedStateChange = ruleOutSymbolOutsideRowOrColumn(boxa, restriction, stepNumber);
					if(causedStateChange) {
						String s = "Column restriction for " + restriction.getRepresentation();
						actions.add(s);
						stateChanges++;
						break;
					}
				}
			}
			if(stateChanges > 0) break;
		}
		
		changedState = (stateChanges > 0);
		return changedState;
	}

	// And the same again for rows ...
	private boolean tryMethod5(int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(RowAssessment row : m_lRows) {
			List<SymbolRestriction> lRestrictions = row.findRestrictedSymbols();
			if(lRestrictions != null) {
				for(SymbolRestriction restriction : lRestrictions) {
					BoxAssessment boxa = getBoxAssessmentForBox(restriction.m_box);
					boolean causedStateChange = ruleOutSymbolOutsideRowOrColumn(boxa, restriction, stepNumber);
					if(causedStateChange) {
						String s = "Row restriction for " + restriction.getRepresentation();
						actions.add(s);
						stateChanges++;
						break;
					}
				}
			}
			if(stateChanges > 0) break;
		}
		
		changedState = (stateChanges > 0);
		return changedState;
	}
	

	// Where n symbols in a row/column/box can only be assigned to the same n cells, then these cells can't be assigned to any other symbols.
	private boolean tryMethod6(int stepNumber, List<String> actions) {
		boolean changedState = false;
		int stateChanges = 0;
		for(CellSetAssessment set : m_lCellSets) {
			List<SymbolSetRestriction> lRestrictedSymbolSets = set.findRestrictedSymbolSets();
			if(lRestrictedSymbolSets != null) {
				for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {						
					for(Cell cell : symbolSetRestriction.m_lCells) {
						CellAssessment ca = assessmentForCell(cell);
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
						CellSetAssessment cseta = assessmentForCellSet(symbolSetRestriction.m_cellSet);
						int causedChange = cseta.ruleOutAllOtherCellsForSymbol(symbolSetRestriction.m_lCells, symbol, stepNumber);
						if(causedChange > 0) {
							stateChanges++;
							String s = "Restriction: " + symbolSetRestriction.getRepresentation();
							actions.add(s);
						}
					}
					
					List<CellSet> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
					for(CellSet cset : lAffectedCellSets) {
						CellSetAssessment cseta = assessmentForCellSet(cset);
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

	private enum CellAssignmentStatus {
		AssignmentAllowed, AssignmentMade, CellAlreadyAssigned, SymbolAlreadyRuledOutForCell, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
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

	boolean ruleOutSymbolOutsideBox(CellSetAssessment csa, SymbolRestriction restriction, int stepNumber) {
		boolean changedState = false;
		// For cells not in the restriction box, rule out the symbol.
		for(Cell cell : csa.getCellSet().getCells()) {
			if(!restriction.m_box.containsCell(cell)) {
				CellAssessment ca = assessmentForCell(cell);
				if(!ca.isRuledOut(restriction.m_symbol)) {
//System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getGridLocationString());				
					ca.ruleOutSymbol(restriction.m_symbol, stepNumber);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}
	
	boolean ruleOutSymbolOutsideRowOrColumn(CellSetAssessment boxcsa, SymbolRestriction restriction, int stepNumber) {
		boolean changedState = false;
		// For cells not in the restriction row/column, rule out the symbol.
		for(Cell cell : boxcsa.getCellSet().getCells()) {
			if(!restriction.m_rowOrColumn.containsCell(cell)) {
				CellAssessment ca = assessmentForCell(cell);
				if(!ca.isRuledOut(restriction.m_symbol)) {
//System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getGridLocationString());				
					ca.ruleOutSymbol(restriction.m_symbol, stepNumber);
					changedState = true;
				}
			}
		}
		
		return changedState;
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
		
		for(CellSetAssessment cellset : m_lCellSets) {
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
			
			List<? extends CellSetAssessment> l = null;
			if(cellSet == 1) l = m_lRows;
			if(cellSet == 2) l = m_lColumns;
			if(cellSet == 3) l = m_lBoxes;
			
//			sb.append("<td>").append(nl);
//			sb.append("<table>").append(nl);
			for(CellSetAssessment cellset : l) {
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
