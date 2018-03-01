package puzzle;

import java.util.Set;
import grid.*;
import solver.*;

// http://www.sudokuwiki.org

public class Puzzle {
	
	public static void main(String args[]) {
				
		InitialGridContentProvider contentProvider = null;
		
		// Read a list of initial grid entries from a file name parameter or from a static variable.
		if(args.length > 0) {
			String puzzleFileName = args[0];
			System.out.println("Reading initial grid from file " + puzzleFileName);
			contentProvider = InitialGridContentProvider.fromFile(puzzleFileName);
			if(contentProvider == null) {
				System.err.println("Failed to read initial grid from file " + puzzleFileName);
			}
		}
		else {
			System.out.println("Reading initial grid from hard-coded puzzle");
			contentProvider = InitialGridContentProvider.fromArray(SampleSudokus.s_times9688);
		}
		
		if(contentProvider == null) return;

		// Only work with basic symbols 1 to 9 for now, expecting a standard 9x9 grid
		SymbolsToUse symbolsToUse = SymbolsToUse.SET_1_TO_9;

		System.out.println("Using initial grid values:");
		System.out.println();
		for(String s : contentProvider.m_dataLines) {
			System.out.println("  " + s);
		}		
		System.out.println();		
		System.out.println("Symbols to use: " + symbolsToUse.toString());
		System.out.println();
				
		Puzzle puzzle = new Puzzle(symbolsToUse);
		InitialGridStatus status = puzzle.loadGivenCells(contentProvider);
		
		if(!status.m_isOK) {
			System.err.println("Error in initial grid values: " + status.m_errorMessage);
		}
		else {
			puzzle.solve();
		}			
	}	
	
	// ================================================================================================
	// ================================================================================================
	
	static int s_expectedSymbolCount = 9;	// Only handle a standard 9x9 grid
	SymbolsToUse m_symbolsToUse;
	Grid9x9 m_grid;
	Solver m_solver;
	
	Puzzle(SymbolsToUse symbols) {
		m_symbolsToUse = symbols;
		m_grid = new Grid9x9();
		m_solver = null;
	}
	
	InitialGridStatus loadGivenCells(InitialGridContentProvider contentProvider) {
		InitialGridStatus status = new InitialGridStatus();
		
		if(m_symbolsToUse.size() != s_expectedSymbolCount) {
			status.setError("Unexpected number of symbols: " + m_symbolsToUse.size() + " instead of " + s_expectedSymbolCount);
			return status;
		}
		
		if(contentProvider.m_dataLines.size() != s_expectedSymbolCount) {
			status.setError("Unexpected number of rows in initial grid: " + contentProvider.m_dataLines.size() + " instead of " + s_expectedSymbolCount);
		}
		else {
			for(int rowNumber = 0; rowNumber < contentProvider.m_dataLines.size(); rowNumber ++) {
				String rowString = contentProvider.m_dataLines.get(rowNumber);
				if(rowString.length() != 9) {
					status.setError("Unexpected number of column values: " + rowString.length() +  " instead of " + s_expectedSymbolCount + " : [" + rowString + "]");	// Not showing the raw input
				}
				else {
					processInitialGridRow(rowNumber, rowString, status);
				}
			}
		}
				
		// Now check for invalid starting positions - a symbol supplied more than once in a particular cellset (row, column or box)
		Set<Cell> badCells = m_grid.isValid();
		if(badCells.size() > 0) {
			String badCellString = "";
			for(Cell cell : badCells) {
				badCellString += (cell.getLocationString() + " ");
			}
			status.setError("Invalid initial grid : see cells " + badCellString);			
		}
		
		return status;
	}
	
	void processInitialGridRow(int rowNumber, String rowString, InitialGridStatus status) {
		for(int columnNumber = 0; columnNumber < rowString.length(); columnNumber ++) {
			char c = rowString.charAt(columnNumber);
			if(c == '.') {
				// Indicates an unknown cell value, to be solved
			}
			else {
				Symbol symbol = m_symbolsToUse.isKnownSymbol(c + "");
				if(symbol != null) {
					applyGivenValueToCell(rowNumber, columnNumber, symbol);
				}
				else {
					status.setError("Unknown symbol in initial grid: " + c);
				}
			}
		}		
	}

	public void applyGivenValueToCell(int rowNumber, int columnNumber, Symbol symbol)
	{
		Cell cell = m_grid.getCellFromGridPosition(rowNumber, columnNumber);
		Assignment assignment = new Assignment(cell, symbol, AssignmentMethod.Given, "", 0);
		cell.assign(assignment);
	}

	// --------------------------------------------------------------------------------------
	
	void solve() {
		m_solver = new Solver(m_grid, m_symbolsToUse);
				
		m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay(), 0);
		m_solver.printGrid(new CellAssessment.CouldBeValueDisplay(), 0);
		m_solver.printCellSets();
		m_solver.printGrid(new CellAssessment.AssignedValueDisplay(), 0);
		
		m_solver.printGrid(new CellAssessment.CellNumberDisplayer());
		m_solver.printGrid(new CellAssessment.BoxNumberDisplayer());
		m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay());
		m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
		m_solver.printCellSets();		

		boolean complete = false;
		boolean changed = true;
		int stepNumber = 0;
		
		while(changed && !complete && stepNumber <= 1000)
		{
			stepNumber++;
			
			System.out.println("==================================================================================================");
			System.out.println("==================================================================================================");
			System.out.println();
			System.out.println("Assignment step: " + stepNumber);

			changed = m_solver.lookForNextAssignment(stepNumber);
			
			m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay(), stepNumber);
			m_solver.printGrid(new CellAssessment.CouldBeValueDisplay(), stepNumber);
			m_solver.printCellSets(stepNumber);
			m_solver.printGrid(new CellAssessment.AssignedValueDisplay(), stepNumber);

			Solver.Stats stats = m_solver.getStats();
			complete = stats.m_complete;

			System.out.println("After step " + stepNumber + ": ");
			System.out.println("- " + stats.m_assignedCells + " assigned cells out of " + stats.m_cellCount + " (" + stats.m_initialAssignedCells + " givens)");
			System.out.println("- " + stats.m_unassignedCells + " unassigned cell" + ((stats.m_unassignedCells == 1) ? "" : "s"));
			System.out.println();
			
			if(complete)
			{
				System.out.println("Puzzle is complete");
				m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
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
				System.out.println("Progress made, continuing puzzle ..");
			}
			System.out.println();
		}
	}
}

class InitialGridStatus {
	boolean m_isOK;
	String m_errorMessage;
	
	InitialGridStatus() {
		m_isOK = true;
		m_errorMessage = "";
	}
	
	void setError(String message) {
		m_isOK = false;
		m_errorMessage = message;
	}
}
