package puzzle;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.List;
import java.util.Set;

import grid.GridLayout;
import grid.Symbol;
import grid.Symbols;
import grid.Grid;
import grid.Cell;
import grid.Assignment;
import grid.AssignmentMethod;
import grid.GridDiagnostics;

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

		// Work out what layout and symbol set are being used for the puzzle.
		int gridRows = contentProvider.m_dataLines.size();
		GridLayout layout = GridLayout.getGridLayoutOfSize(gridRows);
		if(layout == null) {
			System.err.println("Grid layout not recognised rows=" + gridRows);
			return;
		}
		else {
			// Check that each row has the expected number of columns for a square grid
			int rowNumber = 0;
			for(String row : contentProvider.m_dataLines) {
				if(row.length() != gridRows) {
					System.err.println("Grid layout - number of columns (" + row.length()+ ") not equal to number of rows (" + gridRows + ") on row " + (rowNumber+1) + ": [" + row + "]");
					return;					
				}
				rowNumber++;
			}
		}
		
		// And the number of symbols used must be no more than the number of rows (or columns)
		Set<String> symbolsUsed = contentProvider.m_symbolsUsed;
		if(contentProvider.m_symbolsUsed.size() > gridRows) {
			System.err.println("Too many different symbols used (" + symbolsUsed.size() + ") for grid size (" + gridRows + ")");
			System.err.println("Symbols used: " + symbolsUsed.toString());
			return;								
		}
		
		// And the symbols used must belong to one of our known sets of the appropriate size.
		Symbols symbolsToUse = Symbols.matchSymbolSet(gridRows, symbolsUsed);
		if(symbolsToUse == null) {
			System.err.println("Symbols used not from a recognised set for this grid size: " + symbolsUsed.toString());
			return;
		}
				
		System.out.println(".. using a " + layout.description() + " grid layout and " + symbolsToUse.getRepresentation());
		System.out.println(".. using initial grid values:");
		System.out.println();
		for(String s : contentProvider.m_dataLines) {
			System.out.println("  " + s);
		}		
		System.out.println();
	
		// We can now kick off solving of the puzzle.
		Puzzle.Status status = Puzzle.solvePuzzle(symbolsToUse, layout, contentProvider);

		// Report what happened
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
		}
	}	

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

	// --------------------------------------------------------------------------------------
	
	// Look at the 'given' cells we've been provided with and check that they a) tie in with the symbol/grid info 
	// and b) are a valid combination, e.g. they don't have two copies of the same symbol in the same column/row/box.
	private void loadGivenCells() {
		m_status.setInitialGridOK();
		
		if(m_symbolsToUse.size() != m_grid.layout().m_rows) {
			m_status.setInitialGridError("Unexpected number of symbols: " + m_symbolsToUse.size() + " instead of " + m_grid.layout().m_rows);
		}		
		else if(m_contentProvider.m_dataLines.size() != m_grid.layout().m_columns) {
			m_status.setInitialGridError("Unexpected number of rows in initial grid: " + m_contentProvider.m_dataLines.size() + " instead of " + m_grid.layout().m_rows);
		}
		else {
			for(int rowNumber = 0; rowNumber < m_contentProvider.m_dataLines.size(); rowNumber ++) {
				String rowString = m_contentProvider.m_dataLines.get(rowNumber);
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
			if(c == '.') {
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
		m_solver = new Solver(m_grid, m_symbolsToUse);

		// Get the solver to take another deduction step until we've finished or got stuck or gone on too long. 
		while(changed && !complete && stepNumber < maxSteps)
		{
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
		
		// Dump the solver's very detailed diagnostics out as HTML
		writeHTMLFile("logs/diagnostics.html", stepStatus.m_htmlDiagnosticsStyles, stepStatus.m_htmlDiagnostics);		
	}

	// Produce a simple dump of the assignments made to the grid so far
	private void printGridAssignments(int stepNumber) {
		CellDiagnosticsProvider ccd = new GridDiagnostics.AssignedValueDisplay();
		GridFormatter gf = new GridFormatter(m_grid);
		System.out.println();
		System.out.println(gf.formatGrid(ccd, stepNumber));
	}

	private Status getStatus() {
		return m_status;		
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
		
		Status() {
			m_initialGridOK = false;
			m_solved = false;
			m_initialGrid = "";
			m_finalGrid = "";
			m_valid = false;
			m_invalidDetails = "";		
		}
		
		void setInitialGridError(String message) {
			m_initialGridOK = false;
			m_invalidDetails = message;
		}
		
		void setInitialGridOK() {
			m_initialGridOK = true;
			m_invalidDetails = "";			
		}
	}

	void writeHTMLFile(String filename, String styles, String htmlbody) {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(nl);
		sb.append("<head>").append(nl);
		sb.append(styles).append(nl);
		sb.append("</head>").append(nl);
		sb.append("<body>").append(nl);
		sb.append(htmlbody);
		sb.append("</body>").append(nl);
		sb.append("</html>").append(nl);
		
		writeFileAsUTF8(filename, sb.toString(), false);
	}

	public static boolean writeFileAsUTF8(String filename, String s, boolean append) {
	    try {
			FileOutputStream	fos = new FileOutputStream (filename, append);
			OutputStreamWriter	osw = new OutputStreamWriter (fos, "UTF-8");
			BufferedWriter		bw = new BufferedWriter (osw);
	
			bw.write(s, 0, s.length ());
			bw.newLine();
			
			bw.flush();		// Flush the file contents to disk
			bw.close();		// And close it
			return true;
	    }
	    catch(Exception e) {
	        System.err.println("Failed to write to: " + filename + " " + e.getMessage());
	        return false;
	    }
	}	
}
