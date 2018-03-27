package puzzle;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.List;

import grid.GridLayout;
import grid.Symbol;
import grid.Symbols;
import grid.Grid;
import grid.Cell;
import grid.Assignment;
import grid.AssignmentMethod;

import solver.*;
import diagnostics.*;

public class Puzzle {
	
	public static void main(String args[]) {
				
		InitialGridContentProvider contentProvider = null;
		
		// Read a list of initial grid entries from a file name parameter or from a static variable.
		if(args.length > 0 && !args[0].equals("-")) {
			String puzzleFileName = args[0];
			System.out.println("Reading initial grid from file " + puzzleFileName);
			contentProvider = InitialGridContentProvider.fromFile(puzzleFileName);
			if(contentProvider == null) {
				System.err.println("Failed to read initial grid from file " + puzzleFileName);
			}
		}
		else {
			System.out.println("Reading initial grid from hard-coded puzzle");
			String[] sa = SampleSudokus.s_initialValuesLeMondeHard;
			//String[] sa = SampleSudokus.s_times9688;
			contentProvider = InitialGridContentProvider.fromArray(sa);
		}
		
		if(contentProvider == null) return;

		// Only work with basic symbols 1 to 9 for now, expecting a standard 9x9 grid
		Symbols symbolsToUse = Symbols.SYMBOLS_1_TO_9;
		GridLayout layout = GridLayout.GRID9x9;

		//symbolsToUse = Symbols.SYMBOLS_1_TO_6;
		//layout = GridLayout.GRID6x6;

		//symbolsToUse = Symbols.SYMBOLS_A_TO_Y;
		//layout = GridLayout.GRID25x25;

		System.out.println("Using initial grid values:");
		System.out.println();
		for(String s : contentProvider.m_dataLines) {
			System.out.println("  " + s);
		}		
		System.out.println();		
		System.out.println("Symbols to use: " + symbolsToUse.getRepresentation());
		System.out.println();
				
		Puzzle puzzle = new Puzzle(symbolsToUse, layout);
		InitialGridStatus status = puzzle.loadGivenCells(contentProvider);
		
		if(!status.m_isOK) {
			System.err.println("Error in initial grid values: " + status.m_errorMessage);
		}
		else {
			puzzle.solve();
			
			Puzzle.Status finalStatus = puzzle.getStatus();
			
			System.out.println();
			System.out.println("*******************************************************************");
			System.out.println();
			System.out.println("Puzzle was " + (finalStatus.m_solved ? "" : "not ") + "completed:");
			System.out.println();
			System.out.println(finalStatus.m_initialGrid);
			System.out.println();
			System.out.println(finalStatus.m_finalGrid);
			System.out.println();
			System.out.println("Final grid is " + (finalStatus.m_valid ? "valid" : "not valid : " + finalStatus.m_invalidDetails));
		}
	}	
	
	// ================================================================================================
	// ================================================================================================
	
//	static int s_expectedSymbolCount = 9;	// Only handle a standard 9x9 grid
	Symbols m_symbolsToUse;
	Grid m_grid;		// The Grid we want to solve
	Solver m_solver;
	Status m_status;
	
	public Puzzle(Symbols symbols, GridLayout layout) {
		m_symbolsToUse = symbols;
		m_grid = new Grid(layout);
		m_solver = null;
	}
	
	public InitialGridStatus loadGivenCells(InitialGridContentProvider contentProvider) {
		InitialGridStatus status = new InitialGridStatus();
		
		if(m_symbolsToUse.size() != m_grid.layout().m_rows) {
			status.setError("Unexpected number of symbols: " + m_symbolsToUse.size() + " instead of " + m_grid.layout().m_rows);
			return status;
		}
		
		if(contentProvider.m_dataLines.size() != m_grid.layout().m_columns) {
			status.setError("Unexpected number of rows in initial grid: " + contentProvider.m_dataLines.size() + " instead of " + m_grid.layout().m_rows);
		}
		else {
			for(int rowNumber = 0; rowNumber < contentProvider.m_dataLines.size(); rowNumber ++) {
				String rowString = contentProvider.m_dataLines.get(rowNumber);
				if(rowString.length() != m_grid.layout().m_columns) {
					status.setError("Unexpected number of column values: " + rowString.length() +  " instead of " + m_grid.layout().m_columns + " : [" + rowString + "]");	// Not showing the raw input
				}
				else {
					processInitialGridRow(rowNumber, rowString, status);
				}
			}
		}
				
		// Now check for invalid starting positions - a symbol supplied more than once in a particular cellset (row, column or box)
		List<Cell> badCells = m_grid.getListOfIncompatibleCells();
		if(badCells.size() > 0) {
			String badCellString = "";
			for(Cell cell : badCells) {
				badCellString += (cell.getGridLocationString() + " ");
			}
			status.setError("Invalid initial grid : see cells " + badCellString);			
		}
		
		return status;
	}
	
	private void processInitialGridRow(int rowNumber, String rowString, InitialGridStatus status) {
		for(int columnNumber = 0; columnNumber < rowString.length(); columnNumber ++) {
			char c = rowString.charAt(columnNumber);
			if(c == '.') {
				// Indicates an unknown cell value, to be solved
			}
			else {
				Symbol symbol = m_symbolsToUse.isKnownSymbol(c + "");
				if(symbol != null) {
					applyGivenValueToCell(m_grid, columnNumber, rowNumber, symbol);
				}
				else {
					status.setError("Unknown symbol in initial grid: " + c);
				}
			}
		}		
	}

	private void applyGivenValueToCell(Grid grid, int columnNumber, int rowNumber, Symbol symbol)
	{
		Cell cell = grid.getCellFromGridPosition(columnNumber, rowNumber);
		Assignment assignment = new Assignment(cell, symbol, AssignmentMethod.Given, "", 0);
		cell.assign(assignment);
	}

	// --------------------------------------------------------------------------------------
	
	public void solve() {
		
		long startTime = new java.util.Date().getTime();
		
		m_solver = new Solver(m_grid, m_symbolsToUse);
				
//		m_solver.printGrid(new Solver.CellNumberDisplayer());
//		m_solver.printGrid(new Solver.BoxNumberDisplayer());
//		m_solver.printGrid(new Solver.AssignedValueDisplay());
		
//		m_solver.printGrid(m_solver.new CouldBeValueCountDisplay(), 0);
//		m_solver.printGrid(m_solver.new CouldBeValueDisplay(), 0);
//		m_solver.printCellSets();
//		m_solver.printGrid(new Solver.AssignedValueDisplay(), 0);
		
		boolean complete = false;
		boolean changed = true;
		int stepNumber = 0;
		
		GridFormatter gf = new GridFormatter(m_grid);
		String initialGrid = gf.formatCompactGrid(new Solver.AssignedValueDisplay());
		
		while(changed && !complete && stepNumber <= 1000)
		{
			stepNumber++;
			
			System.out.println("==================================================================================================");
			System.out.println("==================================================================================================");
			System.out.println();
			System.out.println("Assignment step: " + stepNumber);

			changed = m_solver.nextStep(stepNumber);
			Grid.Stats stats = m_grid.getStats();
			complete = (stats.m_unassignedCellCount == 0);
			
			printGrid(new Solver.AssignedValueDisplay(), stepNumber);
			if(complete)
			{
				System.out.println("Puzzle is complete");
//				m_solver.printGrid(new Solver.AssignedValueDisplay());
			}
			else if(stepNumber > 1000)
			{
				System.err.println("Puzzle abandoned, too many steps");
			}
			else if(!changed)
			{
				System.err.println("Puzzle abandoned, no more possible changes identified");
			}
			else
			{
//				System.out.println("Progress made, continuing puzzle ..");
			}
//			System.out.println();
		}

		// Check we've not made any invalid assignments
		

		long endTime = new java.util.Date().getTime();
		
		long took = endTime - startTime;
		
		m_status = new Status();
		m_status.m_initialGridStatus = new InitialGridStatus(); 
		m_status.m_solved = complete;
		
		m_status.m_initialGrid = initialGrid;
		GridFormatter gf2 = new GridFormatter(m_grid);
		m_status.m_finalGrid = gf2.formatCompactGrid(new Solver.AssignedValueDisplay());
		
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
		
		m_solver.finaliseDiagnostics(stepNumber, took);
		m_solver.showFinalGrid();
		String htmlbody = m_solver.getHtmlDiagnostics();
		writeHTMLFile("logs/diagnostics.html", htmlbody);		
	}

	public Status getStatus() {
		return m_status;		
	}

	public static class Status {
		public InitialGridStatus m_initialGridStatus;
		public boolean m_solved;
		public String m_initialGrid;
		public String m_finalGrid;
		public boolean m_valid;
		public String m_invalidDetails;
		
		Status() {
			
		}
	}

	public class InitialGridStatus {
		public boolean m_isOK;
		public String m_errorMessage;
		
		InitialGridStatus() {
			m_isOK = true;
			m_errorMessage = "";
		}
		
		void setError(String message) {
			m_isOK = false;
			m_errorMessage = message;
		}
	}
	
	public static Puzzle.Status solve9x9Puzzle(String content) {
		InitialGridContentProvider contentProvider = InitialGridContentProvider.from9x9String(content);
		Puzzle puzzle = new Puzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9);
		InitialGridStatus initialStatus = puzzle.loadGivenCells(contentProvider);
		if(initialStatus.m_isOK) {
			puzzle.solve();
			return puzzle.getStatus();
		}
		else {
			Status status = new Status();
			status.m_initialGridStatus = initialStatus;
			status.m_solved = false;
			status.m_valid = false;
			status.m_invalidDetails = initialStatus.m_errorMessage;
//			status.m_initialGrid = puzzle.m_solver.formatCompactGrid(new CellAssessment.AssignedValueDisplay());	// null pointer error
			status.m_finalGrid = null;
			return status;
		}		
	}
	
	private static String s_divider = "-----------------------------------";

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

	void writeHTMLFile(String filename, String htmlbody) {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(nl);
		sb.append("<head>").append(nl);
		sb.append(getStyles()).append(nl);
		sb.append("</head>").append(nl);
		sb.append("<body>").append(nl);
		sb.append(htmlbody);
		sb.append("</body>").append(nl);
		sb.append("</html>").append(nl);
		
		writeFileAsUTF8(filename, sb.toString(), false);
	}

	String getStyles() {
		String nl = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		
		sb.append("<style>").append(nl);
		sb.append("body {").append(nl);
		sb.append("    font-family: Tahoma, Geneva, sans-serif;").append(nl);			// https://www.w3schools.com/cssref/css_websafe_fonts.asp
		sb.append("}").append(nl);
		sb.append(".gridouter {").append(nl);
		sb.append("    border: 6px solid gray;").append(nl);
		sb.append("    border-collapse: collapse;").append(nl);
		sb.append("}").append(nl);
		sb.append(".gridseparatorrow {").append(nl);
		sb.append("    border-top: 4px solid grey;").append(nl);
		sb.append("}").append(nl);
		sb.append(".gridseparatorcolumn {").append(nl);
		sb.append("    border-left: 4px solid grey;").append(nl);
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
		sb.append(".gridcell {").append(nl);
		sb.append("    width: 20px;").append(nl);
		sb.append("    height: 20px;").append(nl);
		sb.append("    text-align: center;").append(nl);
		sb.append("}").append(nl);
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
		sb.append(".highlight {").append(nl);
		sb.append("    background-color: yellow;").append(nl);
		sb.append("}").append(nl);
		sb.append(".given {").append(nl);
		sb.append("    background-color: lightsteelblue;").append(nl);
		sb.append("}").append(nl);
		sb.append(".previouslyassigned {").append(nl);
		sb.append("    background-color: aquamarine;").append(nl);
		sb.append("}").append(nl);
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
		

		
		
//		sb.append("table { border-collapse: collapse;}").append(nl);
//		sb.append("table, th, td { border: 1px solid black; }").append(nl);
		sb.append(".observation { color: red; }").append(nl);
		sb.append("</style>").append(nl);
		
		return sb.toString();
	}
	public static boolean writeFileAsUTF8(String filename, String s, boolean append)
	{
	    try
	    {
			FileOutputStream	fos = new FileOutputStream (filename, append);
			OutputStreamWriter	osw = new OutputStreamWriter (fos, "UTF-8");
			BufferedWriter		bw = new BufferedWriter (osw);
	
			bw.write (s, 0, s.length ());
			bw.newLine ();
			
			bw.flush ();		// Flush the file contents to disk
			bw.close ();		// And close it
			return true;
	    }
	    catch(Exception e)
	    {
	        System.err.println("Failed to write to: " + filename + " " + e.getMessage());
	        return false;
	    }
	}	
}

