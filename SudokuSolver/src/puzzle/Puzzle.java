package puzzle;

import java.util.List;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;

import grid.*;
import solver.*;
import diagnostics.*;

/**
 * Main entry point for solving a Sudoku puzzle.
 * 
 *  The main method allows Sudokus to be read from a text file. Or the solvePuzzle method can be invoked.
 */

public class Puzzle {
	
	public static void main(String args[]) {
				
		InitialGridContentProvider contentProvider = null;
		
		// Read the initial grid entries from the named file provided as a parameter 
		if(args.length > 0 && !args[0].equals("-")) {
			String puzzleFileName = args[0];
			System.out.println(".. reading initial grid from file " + puzzleFileName);
			contentProvider = InitialGridContentProvider.fromFile(puzzleFileName);
			if(contentProvider == null) {
				System.err.println("Failed to read initial grid from file " + puzzleFileName);
			}
		}
		// .. or from a static variable if no file name parameter given.
		else {
			System.out.println(".. reading initial grid from hard-coded sample puzzle");
			String[] sa = SampleSudokus.SAMPLE1;
			contentProvider = InitialGridContentProvider.fromArray(sa);
		}
		
		if(contentProvider == null) return;

		// Use the content to work out grid layout and the set of symbols being used.
		GridLayout layout = contentProvider.workOutGridLayout();
		Symbols symbolsToUse = contentProvider.workOutSymbolsToUse();

		if(layout == null || symbolsToUse == null) return;

		// We've got a valid puzzle to try and solve.
		System.out.println(".. using a " + layout.description() + " grid layout and " + symbolsToUse.getRepresentation());
		System.out.println(".. using initial grid values:");
		System.out.println();
		for(String s : contentProvider.dataLines()) {
			System.out.println("  " + s);
		}		
		System.out.println();
	
		Puzzle.Status status = Puzzle.solvePuzzle(symbolsToUse, layout, contentProvider);

		if(!status.m_initialGridOK) {
			System.err.println("Error in initial grid values: " + status.m_invalidDetails);			
		}
		else {		
			System.out.println();
			System.out.println("*******************************************************************");
			System.out.println();
			System.out.println("The puzzle was " + (status.m_solved ? "" : "not ") + "completed.");
			System.out.println();
			System.out.println("Initial grid:");
			System.out.println();
			System.out.println(status.m_initialGrid);
			System.out.println();
			System.out.println("Final grid:");
			System.out.println();
			System.out.println(status.m_finalGrid);
			System.out.println();
			System.out.println("The final grid is " + (status.m_valid ? "valid" : "not valid : " + status.m_invalidDetails));
			System.out.println();
			
			// Dump the solver's very detailed diagnostics out as HTML if there's a folder called 'logs' available to put it in.
			String logsFolderName = "logs";
			String diagnosticsFilename = logsFolderName + "/diagnostics.html";
			File logsFolder = new File(logsFolderName);
			if(logsFolder.exists() && logsFolder.isDirectory() && logsFolder.canWrite()) {
				System.out.println("Detailed diagnostics are available in HTML file: " + diagnosticsFilename);
				writeFile(diagnosticsFilename, status.m_htmlDiagnostics);
			}
			else {
				System.out.println("No detailed diagnostics produced - no " + logsFolderName + " folder present");
			}
		}
	}	

	private static void writeFile(String filename, String s) {
	    try (BufferedWriter bw = Files.newBufferedWriter(new File(filename).toPath(), StandardCharsets.UTF_8)) {
			bw.write(s, 0, s.length());
			bw.newLine();
	    }
	    catch(Exception e) {
	        System.err.println("Failed to write to: " + filename + " " + e.getMessage());
	    }
	}	

	// ================================================================================================
	// ================================================================================================

	/**
	 * Programmatic entry point for solving a puzzle
	 * 
	 *  The main method allows Sudokus to be read from a text file. Or the solvePuzzle method can be invoked.
	 *  
	 * @param symbols Which set of symbols is being used for the puzzle ?
	 * @param layout What grid layout is being used for the puzzle ?
	 * @param contentProvider Provides the initial grid values for the puzzle.
	 * @return Status information about how solution processing worked.
	 */
	public static Puzzle.Status solvePuzzle(Symbols symbols, GridLayout layout, InitialGridContentProvider contentProvider) {
		Puzzle puzzle = new Puzzle(symbols, layout, contentProvider);
		puzzle.solve();
		return puzzle.getStatus();
	}
	
	// ================================================================================================
	// ================================================================================================
	
	private Symbols m_symbolsToUse;
	private Grid m_grid;
	private Solver m_solver;
	private InitialGridContentProvider m_contentProvider;
	private Status m_status;
	
	// Create a new puzzle from a combination of the set of symbols being used, the grid layout info (e.g. 9x9)
	// and the initial 'given' cells.
	private Puzzle(Symbols symbols, GridLayout layout, InitialGridContentProvider contentProvider) {
		m_symbolsToUse = symbols;
		m_grid = new Grid(layout);
		m_contentProvider = contentProvider;
		m_solver = null;
		m_status = new Status();
		loadGivenCells();
	}

	private Status getStatus() {
		return m_status;		
	}

	// --------------------------------------------------------------------------------------
	
	// Look at the 'given' cells we've been provided with and check that they a) tie in with the symbol/grid info 
	// and b) are a valid combination, e.g. they don't have two copies of the same symbol in the same column/row/box.
	private void loadGivenCells() {
		m_status.setInitialGridOK();
		
		List<String> dataLines = m_contentProvider.dataLines();
		
		if(m_symbolsToUse.size() != m_grid.layout().m_rows) {
			m_status.setInitialGridError("Unexpected number of symbols: " + m_symbolsToUse.size() + " instead of " + m_grid.layout().m_rows);
		}
		else if(dataLines.size() != m_grid.layout().m_columns) {
			m_status.setInitialGridError("Unexpected number of rows in initial grid: " + dataLines.size() + " instead of " + m_grid.layout().m_rows);
		}
		else {
			for(int rowNumber = 0; rowNumber < dataLines.size(); rowNumber ++) {
				String rowString = dataLines.get(rowNumber);
				if(rowString.length() != m_grid.layout().m_columns) {
					m_status.setInitialGridError("Unexpected number of column values: " + rowString.length() +  " instead of " + m_grid.layout().m_columns + " : [" + rowString + "]");	// Not showing the raw input
				}
				else {
					processInitialGridRow(rowNumber, rowString);
				}
			}
		}

		// Keep a formatted copy of the initial grid for dumping out at the end.
		GridFormatter gf = new GridFormatter(m_grid);
		m_status.m_initialGrid = gf.formatCompactGrid(new GridDiagnostics.AssignedValueDisplay());
		
		if(m_status.m_initialGridOK) {
			checkValidStartingPosition();
		}
		
		return;
	}
	
	// Look at the symbols provided for the row, and apply them to the grid as 'given' values unless
	// they indicate an unknown value (by a '.')
	private void processInitialGridRow(int rowNumber, String rowString) {
		for(int columnNumber = 0; columnNumber < rowString.length(); columnNumber ++) {
			char c = rowString.charAt(columnNumber);
			if(c == InitialGridContentProvider.UNKNOWN_CELL_VALUE) {
				// Indicates an unknown cell value, to be solved
			}
			else {
				Symbol symbol = m_symbolsToUse.isKnownSymbol(c + "");
				if(symbol != null) {
					Cell cell = m_grid.getCellFromGridPosition(columnNumber, rowNumber);
					cell.assign(new Assignment(cell, symbol, AssignmentMethod.Given, "", 0));
				}
				else {
					m_status.setInitialGridError("Unknown symbol in initial grid: " + c);
				}
			}
		}		
	}

	// Check for invalid starting positions - a symbol supplied more than once in a particular cellset (row, column or box)
	private void checkValidStartingPosition() {
		List<Cell> badCells = m_grid.getListOfIncompatibleCells();
		if(badCells.size() > 0) {
			String badCellString = "";
			for(Cell cell : badCells) {
				badCellString += (cell.getGridLocationString() + " ");
			}
			m_status.setInitialGridError("Invalid initial grid : see cells " + badCellString);			
		}
	}
	
	// --------------------------------------------------------------------------------------
	
	// Main puzzle solution loop, invoking the Solver object a step at a time to try to deduce
	// what the rest of the Sudoku grid should contain.
	
	private void solve() {
		
		if(!m_status.m_initialGridOK) return;
		
		boolean complete = false;
		boolean changed = true;
		int stepNumber = 0;		
		int maxSteps = 1000;
		Solver.SolutionStepStatus stepStatus = null;
		m_solver = new Solver(m_grid, m_symbolsToUse, m_contentProvider.sourceInfo());

		// Get the solver to take another deduction step until we've finished or got stuck or gone on too long. 
		while(changed && !complete && stepNumber < maxSteps) {
			stepNumber++;
			
			System.out.println("==================================================================================================");
			System.out.println();
			System.out.println("Assignment step: " + stepNumber + " ... ");
			System.out.println();

			stepStatus = m_solver.nextStep(stepNumber);
			for(String action : stepStatus.m_actions) {
				System.out.println("- " + action);
			}

			for(String event : stepStatus.m_unexpectedEvents) {
				System.err.println("- " + event);
			}
			
			if(stepStatus.m_isComplete) {
				printGridAssignments(stepNumber);
				System.out.println();
				System.out.println("Puzzle is complete");
				complete = true;
			}
			else if(!stepStatus.m_changedState) {
				System.out.println();
				System.err.println("Puzzle abandoned, no more possible changes identified during step " + stepNumber);
				changed = false;
			}
			else {
				printGridAssignments(stepNumber);
			}

			if(stepNumber == maxSteps) {
				System.out.println();
				System.err.println("Puzzle abandoned after " + stepNumber + " steps");
			}
		}

		// Format the final grid for display
		GridFormatter gf2 = new GridFormatter(m_grid);
		m_status.m_finalGrid = gf2.formatCompactGrid(new GridDiagnostics.AssignedValueDisplay());
		
		// Check we've not made any invalid assignments		
		List<Cell> badCells = m_grid.getListOfIncompatibleCells();
		if(badCells.size() > 0) {
			String badCellString = "";
			for(Cell cell : badCells) {
				badCellString += (cell.getGridLocationString() + " ");
			}
			m_status.m_valid = false;
			m_status.m_invalidDetails = "Invalid final grid : see cells " + badCellString;			
		}
		else {
			m_status.m_valid = true;
		}			

		// Record whether we've completed the puzzle or not.
		m_status.m_solved = complete;
		m_status.m_htmlDiagnostics = stepStatus.m_htmlDiagnostics;
	}

	// Produce a simple dump of the assignments made to the grid so far
	private void printGridAssignments(int stepNumber) {
		CellDiagnosticsProvider ccd = new GridDiagnostics.AssignedValueDisplay();
		GridFormatter gf = new GridFormatter(m_grid);
		System.out.println();
		System.out.println(gf.formatGrid(ccd));
	}

	// --------------------------------------------------------------------------------------
	
	// Class to record status info about how we got on trying to solve the puzzle.
	
	public static class Status {
		public boolean m_initialGridOK;		// Was the initial Grid valid ?
		public boolean m_solved;			// Did we manage to fill in all the missing cells ? 
		public boolean m_valid;				// Are the assignments to final grid non-contradictory (even if not complete) ?
		public String m_initialGrid;		// Shows the initial grid assignments
		public String m_finalGrid;			// Shows the final grid assignments
		public String m_invalidDetails;		// Records details of errors in the initial or final grid
		public String m_htmlDiagnostics;	// HTML page content with detailed processing info
		
		Status() {
			m_initialGridOK = false;
			m_solved = false;
			m_initialGrid = "";
			m_finalGrid = "";
			m_valid = false;
			m_invalidDetails = "";
			m_htmlDiagnostics = "";
		}
		
		void setInitialGridError(String message) {
			m_initialGridOK = false;
			m_invalidDetails = message;
			m_valid = false;
		}
		
		void setInitialGridOK() {
			m_initialGridOK = true;
			m_invalidDetails = "";			
		}
	}
}
