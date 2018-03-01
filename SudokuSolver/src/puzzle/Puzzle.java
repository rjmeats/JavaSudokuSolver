package puzzle;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import diagnostics.TheLogger;
import grid.*;
import solver.*;

// http://www.sudokuwiki.org

public class Puzzle {
	
	public static Logger L = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static void main(String args[]) {
		List<String> initialGridValues = null;

		// Read a list of initial grid entries from a file name parameter or from a static variable.
		if(args.length > 0) {
			String puzzleFileName = args[0];
			System.out.println("Reading initial grid from file " + puzzleFileName);
			initialGridValues = readInitialGridFromFile(puzzleFileName);
			if(initialGridValues == null) {
				System.err.println("Failed to read initial grid from file " + puzzleFileName);
			}
		}
		else {
			System.out.println("Reading initial grid from hard-coded puzzle");
			initialGridValues = Arrays.asList(SampleSudokus.s_times9688);
		}
		
		if(initialGridValues == null) return;

		System.out.println("Using initial grid values:");
		System.out.println();
		for(String s : initialGridValues) {
			System.out.println("  " + s);
		}		
		System.out.println();
		
		// Only work with basic symbols 1 to 9 for now, expecting a standard 9x9 grid
		SymbolsToUse symbolsToUse = SymbolsToUse.SET_1_TO_9;

		System.out.println("Symbols to use: " + symbolsToUse.toString());
		System.out.println();
				
		try {
			TheLogger.setup();
			L.setLevel(Level.ALL);
		} catch(IOException e) {
			System.err.println("Error setting up logger: " + e.getMessage());
			return;
		}
		
		Puzzle puzzle = new Puzzle(symbolsToUse);
		InitialGridStatus status = puzzle.loadGivenCells(initialGridValues);
		
		if(!status.m_isOK) {
			System.err.println("Error in initial grid: " + status.m_errorMessage);
			L.info("Error in initial grid: " + status.m_errorMessage);
		}
		else {
			puzzle.solve();
		}			
	}	
	
	static List<String> readInitialGridFromFile(String fileName) {		
		List<String> lines = null;		
		File f = new File(fileName);
		
		if(!(f.exists() && f.isFile() && f.canRead())) {
			System.err.println("Unable to read file " + fileName);
		}
		else {
			
			lines = new ArrayList<>();
			
			try {
				Scanner sc = new Scanner(f);
				while(sc.hasNextLine()) {
					lines.add(sc.nextLine());
				}
				sc.close();
			} catch (FileNotFoundException e) {
				// Shouldn't get here
			}
		}

		return lines;
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
	
	InitialGridStatus loadGivenCells(List<String> inputValueRows) {
		InitialGridStatus status = new InitialGridStatus();
		
		if(m_symbolsToUse.size() != s_expectedSymbolCount) {
			status.setError("Unexpected number of symbols: " + m_symbolsToUse.size() + " instead of " + s_expectedSymbolCount);
			return status;
		}
		
		List<String> gridRows = removeCommentLines(removeBlankLines(inputValueRows));	// ???? Stream/filter
		
		if(gridRows.size() != s_expectedSymbolCount) {
			status.setError("Unexpected number of rows in initial grid: " + gridRows.size() + " instead of " + s_expectedSymbolCount);
		}
		else {
			for(int rowNumber = 0; rowNumber < gridRows.size(); rowNumber ++) {
				String rowString = gridRows.get(rowNumber);
				String despacedRowString = removeSpacing(rowString);
				if(despacedRowString.length() != 9) {
					status.setError("Unexpected number of column values: " + despacedRowString.length() +  " instead of " + s_expectedSymbolCount + " : [" + rowString + "]");
				}
				else {
					processInitialGridRow(rowNumber, despacedRowString, status);
				}
			}
		}
				
		// Now check for invalid starting positions - symbol applied more than once to a cellset
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
	
	// Ignore blank lines in the strings forming the initial grid
	private static List<String> removeBlankLines(List<String> l) {
		List<String> lOut = new ArrayList<>();
		for(String s : l) {
			if(s.trim().length() > 0) {
				lOut.add(s);
			}
		}
		
		return lOut;
	}

	// Ignore comment lines in the strings forming the initial grid		
	private static List<String> removeCommentLines(List<String> l) {
		char commentChar = '#';		
		List<String> lOut = new ArrayList<>();
		for(String s : l) {
			String trimS = s.trim();
			if(trimS.length() > 0 && trimS.charAt(0) != commentChar) {
				lOut.add(s);
			}
		}
		
		return lOut;
	}

	private static String removeSpacing(String rowString) {
		return rowString.replaceAll("\\s+", "");
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
		Puzzle.L.info("Applying given value : " + symbol.getRepresentation() + " to cell in row " + rowNumber + ", column " + columnNumber);
		Cell cell = m_grid.getCellFromGridPosition(rowNumber, columnNumber);
		Assignment assignment = new Assignment(cell, symbol, AssignmentMethod.Given, "", 0);
		cell.assign(assignment);
	}


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
			L.info("Starting step " + stepNumber + " ...");
			
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
				L.info("Puzzle completed");
				m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
			}
			else if(stepNumber > 1000)
			{
				System.out.println("Puzzle abandoned, too many steps");
				L.info("Puzzle abandoned, too many steps");					
			}
			else if(!changed)
			{
				System.out.println("Puzzle abandoned, no changes identified");
				L.info("Puzzle abandoned, , no changes identified");										
			}
			else
			{
				System.out.println("Progress made, continuing puzzle ..");
				L.info("Progress made, continuing puzzle ..");															
			}
			System.out.println();
		}
	
		L.info("Puzzle run ended");
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
