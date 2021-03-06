package solver;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import grid.Cell;
import grid.CellSet;
import grid.Row;
import grid.Symbol;
import grid.Symbols;
import grid.Column;
import grid.Assignment;
import grid.Box;
import grid.Grid;

/**
 * Coordinates an attempt to solve a Sudoku problem using step by step logical reasoning.
 */

public class Solver {

	// The grid to be worked on using the specified set of symbols. Initially the grid just has
	// the 'given' assignments present, and is then updated as the solution attempt progresses.
	private Grid m_grid;	
	private Symbols m_symbols;
	
	private String m_puzzleSource;					// Identifies the puzzle being solved.
	private java.util.Date m_startTime;
	private long m_combinedTookTime;				// Milliseconds
	
	// If we've completed the grid, don't do any more work if invoked again.
	private boolean m_completed;
	
	// Keep track of 'assessments' relating to the grid, recording what is ruled in/out as we
	// make deductions:
	// - assessment info for each cell in the grid
	// - assessment info for each cellset (column, row, box) in the grid
	private List<CellAssessment> m_lCellAssessments;
	private List<CellSetAssessment> m_lCellSetAssessments;
	
	// Maps to make it easy to go from a basic grid object to its corresponding assessment object.
	private HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	private HashMap<CellSet, CellSetAssessment> m_cellSetAssessmentsMap;

	// 'Method' objects which perform different types of deductions.
	private List<Method> m_methods;
	
	// List of unexpected events recorded while trying to solve a puzzle, indicating some sort of
	// logic error has arisen.
	private List<String> m_unexpectedEvents;		// For the entire attempt

	// Object to produce detailed diagnostic output, including an HTML page.
	private SolverDiagnostics m_diagnostics;
	
	public Solver(Grid grid, Symbols symbols, String puzzleSource) {
		m_grid = grid;
		m_symbols = symbols;
		m_puzzleSource = puzzleSource;

		m_completed = false;

		m_lCellAssessments = new ArrayList<>();
		m_lCellSetAssessments = new ArrayList<>();

		m_cellAssessmentsMap = new HashMap<>();
		m_cellSetAssessmentsMap = new HashMap<>();
		
		// Set up cellset assessment objects for each column, row and box
		for(Column column : m_grid.columns()) {
			ColumnAssessment columnAssessment = new ColumnAssessment(column, symbols);
			m_lCellSetAssessments.add(columnAssessment);
			m_cellSetAssessmentsMap.put(column, columnAssessment);
		}

		for(Row row : m_grid.rows()) {
			RowAssessment rowAssessment = new RowAssessment(row, symbols);
			m_lCellSetAssessments.add(rowAssessment);
			m_cellSetAssessmentsMap.put(row, rowAssessment);
		}
		
		for(Box box: m_grid.boxes()) {
			BoxAssessment boxAssessment = new BoxAssessment(box, symbols);
			m_lCellSetAssessments.add(boxAssessment);
			m_cellSetAssessmentsMap.put(box, boxAssessment);
		}

		// And an assessment object for each cell
		for(Cell cell : m_grid.cells()) {
			setUpCellAssessment(cell);
		}		

		m_methods = new ArrayList<>();
		m_methods.add(new Method1(this));
		m_methods.add(new Method2(this));
		m_methods.add(new Method3(this));
		m_methods.add(new Method4(this));

		m_unexpectedEvents = new ArrayList<>();
		m_startTime = new java.util.Date();
		m_combinedTookTime = 0;
		
		m_diagnostics = new SolverDiagnostics(this, m_grid, m_symbols, m_puzzleSource);

		// The grid already has 'given' assignments in place. We need to go through these propagate this information to the assessment objects that we just set up to get
		// to a full starting position.
		int initialAssignmentCount = 0;
		for(CellAssessment ca : m_lCellAssessments) {
			Cell cell = ca.cell();
			if(cell.isAssigned()) {
				spreadAssignmentImpact(ca, cell.assignment());
				initialAssignmentCount++;
			}
		}
		List<String> actions = new ArrayList<>();
		actions.add("assigned 'given' values for " + initialAssignmentCount + " cells");
		
		// Produce initial diagnostics recording what assessments we've got from applying the 'given' cell assignments.
		int stepNumber = -1;
		boolean changedState = true;
		List<String> unexpectedEvents = new ArrayList<>();
		m_diagnostics.produceDiagnosticsAfterStep(stepNumber, changedState, actions, unexpectedEvents, false);
		
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
	
	List<CellAssessment> cellAssessments() {
		return m_lCellAssessments;
	}
	
	List<CellSetAssessment> cellSetAssessments() {
		return m_lCellSetAssessments;
	}

	Symbols symbols() {
		return m_symbols;
	}

	List<Method> methods() {
		return m_methods;
	}

	java.util.Date startTime() {
		return m_startTime;
	}
	// -----------------------------------------------------------------------------------------

	/**
	 * Get the solver to perform another round of deductions if it can
	 * 
	 * @param stepNumber What is the current step number ?
	 * @return True if a further deduction (assignment or ruling out) has been made; false if no
	 * 		   further progress has been made and we're effectively stuck (or finished). 
	 */
	public SolutionStepStatus nextStep(int stepNumber) {
		long stepStartTime = new java.util.Date().getTime();
		SolutionStepStatus status = new SolutionStepStatus(stepNumber);
		status.m_changedState = false;
		
		// Don't do any more if this grid was completed already before this step started.
		if(!m_completed) {
				
			// Try the available methods until one of them deduces something.
			boolean forceDiagnostics = false;
			for(Method method : m_methods) {
				if(!status.m_changedState) {
					try {
						method.m_calledCount++;
						status.m_changedState = method.applyMethod(stepNumber, status.m_actions);
						if(status.m_changedState) {
							forceDiagnostics = method.isComplexApproach();
							method.m_usefulCount++;
							if(method.m_firstUsefulStepNumber == -1) {
								method.m_firstUsefulStepNumber = stepNumber;
							}
						}
					} 
					catch(IllegalAssignmentException e) {
						status.m_unexpectedEvents.add(e.getMessage());
						System.err.println(e.getMessage());
					}				
				}			
			}			
			m_diagnostics.produceDiagnosticsAfterStep(status.m_stepNumber, status.m_changedState, status.m_actions, status.m_unexpectedEvents, forceDiagnostics);		
			m_unexpectedEvents.addAll(status.m_unexpectedEvents);
			
			long stepEndTime = new java.util.Date().getTime();
			status.m_stepTookTime = stepEndTime - stepStartTime;
		} 
		else {
			status.m_stepTookTime = 0;			
		}

		m_combinedTookTime += status.m_stepTookTime;
		status.m_gridStats = m_grid.stats();
		status.m_isComplete = (status.m_gridStats.m_unassignedCellCount == 0);

		if(!m_completed && (status.m_isComplete || !status.m_changedState)) {
			// Just completed the puzzle.
			m_completed = true;
			m_diagnostics.produceFinalDiagnostics(status, stepNumber, m_combinedTookTime, m_unexpectedEvents);
		}

		status.m_htmlDiagnostics = m_diagnostics.htmlDiagnostics(); 

		return status;
	}

	// -----------------------------------------------------------------------------------------

	// Various functions that the 'Methods' classes can invoke when necessary to make assignments and/or rule out
	// possibilities. 

	/**
	 * Check that an assignment we're about to make is valid
	 * 
	 * @param ca CellAssessment object relating to the cell being assigned to
	 * @param assignment Assignment object describing the assignment
	 * @return AssignmentAllowed indicates the assignment is OK, anything else indicates a logic error. 
	 */
	static CellAssignmentStatus checkCellCanBeAssigned(CellAssessment ca, Assignment assignment) {
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

	/**
	 * Makes an assignment of a symbol to a cell as long as there are no unexpected conflicts, 
	 * and propogates impact to determine what other possibilities are now ruled out.
	 *  
	 * @param assignment Detailed of the assignment being made.
	 * @return Returns AssignmentMade if successful, otherwise an error status value is returned.
	 * @throws IllegalAssignmentException A cell-to-symbol assignment failed.
	 */
	CellAssignmentStatus performAssignment(Assignment assignment) throws IllegalAssignmentException {
		CellAssessment ca = assessmentForCell(assignment.cell());
		CellAssignmentStatus status = checkCellCanBeAssigned(ca, assignment);
		if(status == CellAssignmentStatus.AssignmentAllowed) {
			assignment.cell().assign(assignment);
			spreadAssignmentImpact(ca, assignment);
			status = CellAssignmentStatus.AssignmentMade;
		}
		else {
			String s = "Unexpected assignment failure at step " + assignment.stepNumber() + " : " + status.name() + " : " + assignment.description();
			throw new IllegalAssignmentException(s, status);
		}
		
		return status;
	}
	
	// ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	// 	
	// Spreading the impact of an assignment of a cell to a symbol impacts the possibilities for lots of related cells and the cellsets
	// they belong to, with these impacts being recorded in the relevant CellAssessment and CellSetAssessment objects.
	//
	//	Suppose for a 9x9 grid we have made an assignment of the symbol '1' to cell [6,2] (i.e. cell number 15, which is in column 6, row 2), 
	//	shown in the diagram below as '=1'
	//
	// 			C1	C2	C3		C4	C5	C6		C7	C8	C9
	//		R1					#	#	#
	//		R2	#	#	#		#	#	=1		#	#	#
	//		R3					#	#	#
	//	
	//		R4							#
	//		R5							#
	//		R6							#
	//	
	//		R7							#
	//		R8							#
	//		R9							#
	//
	//  Step 1:
	//  The first thing to record relates to cell [6,2] - we can record that all other symbols except '1' are ruled out for this cell.
	//  This is done via a call to CellAssessment.assignmentMade
	//
	//  Step 2:
	//  The assignment also means that we can update the assessment info for the three cellsets which cell [6,2] resides in: Column 6, Row 2 and Box 2.
	//	For each of these three CellSetAssessments, we can record that:
	//	- the symbol '1' has been assigned for the cellset, and is in cell 15
	// 	- for the other symbols ('2' to '9'), cell 15 can be ruled out as a possibility
	//  This is done via calls to CellSetAssessment.assignmentMade
	//
	//	And there's more work to be done ruling out further possibilities:
	// 
	//	Step 3:
	//	We can also update the CellAssessments for each cell which shares a cellset with cell [6,2] - cells in Column 6 or Row 2 or Box 2. These
	//	20 cells are marked with '#' in the diagram above. None of these cells can be assigned the symbol '1'.
	//	This is done via calls to CellAssessment.ruleOutSymbol
	//
	//	Step 4:
	//	And finally, each of the cells which shares a cellset with cell 6,2], the '#' cells in the diagram, is associated with 3 cellsets. For
	//	each of the cellsets associated with each '#' cell, we can rule out the '#' cell as being the symbol '1' for the cell set. In the
	//	diagram above, this applies to every column cellset, every row cellset, and boxes 1,2,3,5 and 8.
	//	This is done via calls to CellSetAssessment.ruleOutCellForSymbol 
	// 	
	// ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	
	private void spreadAssignmentImpact(CellAssessment cellAssessment, Assignment assignment) {
		cellAssessment.assignmentMade(assignment.symbol(), assignment.stepNumber());	// Step 1 in the description above		
		for(CellSetAssessment cellSetAssessment : cellAssessment.cellSetAssessments()) {
			spreadAssignmentImpactToRelatedCellSets(cellSetAssessment, assignment);			
		}
	}
	
	private void spreadAssignmentImpactToRelatedCellSets(CellSetAssessment cellSetAssessment, Assignment assignment) {
		cellSetAssessment.assignmentMade(assignment.symbol(), assignment.cell(), assignment.stepNumber());	// Step 2 in the description above	
		// Go through the other cells in this cell-set, and rule out this symbol from being assigned to those cells 
		for(Cell otherCell : cellSetAssessment.cellSet().cells()) {
			if(otherCell != assignment.cell()) {
				spreadRulingOutImpact(otherCell, assignment.symbol(), assignment.stepNumber());
			}
		}		
	}	

	// Also called from within a Method class, so not private, and returns an indication of if any of the ruling out was 
	// new.
	int spreadRulingOutImpact(Cell cell, Symbol symbol, int stepNumber) {
		int changeCount = 0;
		CellAssessment cellAssessment = assessmentForCell(cell);
		changeCount += cellAssessment.ruleOutSymbol(symbol, stepNumber);		// Step 3 in the description above.
		for(CellSetAssessment cellSetAssessment : cellAssessment.cellSetAssessments()) {
			changeCount += cellSetAssessment.ruleOutCellForSymbol(cell, symbol, stepNumber);	// Step 4 in the description above.
		}		
		return changeCount;
	}
	
	// -----------------------------------------------------------------------------------------
	
	/**
	 * Class to return the state of the solution process after each step
	 */
	public static class SolutionStepStatus {
		public int m_stepNumber;
		public boolean m_isComplete;
		public boolean m_changedState;
		public Grid.Stats m_gridStats;
		public List<String> m_actions = new ArrayList<>();
		public List<String> m_unexpectedEvents = new ArrayList<>();
		public long m_stepTookTime;
		
		public String m_htmlDiagnostics;
		
		SolutionStepStatus(int stepNumber) {
			m_stepNumber = stepNumber;
			m_actions = new ArrayList<>();
			m_unexpectedEvents = new ArrayList<>();
			m_htmlDiagnostics = "";
		}
	}
}

// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------

/**
 * Possible results of trying to assign a symbol to a cell.
 */
enum CellAssignmentStatus {
	AssignmentAllowed, AssignmentMade, CellAlreadyAssigned, SymbolAlreadyRuledOutForCell, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}

// -----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

/**
 * Exception thrown when we try to make an invalid assignment to a cell.
 */
@SuppressWarnings("serial")
class IllegalAssignmentException extends Exception {
	
	CellAssignmentStatus m_badStatus;
	IllegalAssignmentException(String s, CellAssignmentStatus badStatus) {
		super(s);
		m_badStatus = badStatus;
	}
}

