package puzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import diagnostics.TheLogger;
import grid.*;
import solver.*;


// http://www.sudokuwiki.org

public class Puzzle {

	
	static String[] s_initialValues1 = {
			
					"2..    .4.    .6.",			
					"74.    ..9    8.2",			
					".9.    682    73.",			
												"",
												"",
					".2.    .76    .4.",			
					"..1    .5.    3..",			
					".5.    29.    .7.",			
												"",
												"",
					".69    725    .1.",			
					"5.2    9..    .87",			
					".3.    .6.    ..5",			
	};
	
	static String[] s_initialValues2 = {
			
			"..6    5.8    2..",			
			"5..    2.9    ..7",			
			"..2    .1.    4..",			
										"",
										"",
			"..3    ...    9..",			
			"...    6.2    ...",			
			"4.8    ...    5.1",			
										"",
										"",
			".3.    ...    .6.",			
			".5.    8.1    .7.",			
			"1..    ...    ..4",		
	};

	// www.sudokuwiki.org gets a bit further with some advanced strategies, but then stops. Apparently there are 149 solutions!
	static String[] s_initialValuesLeMondeHard = {
			
			"8..    ..1    2..",			
			".75    ...    ...",			
			"...    .5.    .64",			
										"",
										"",
			"..7    ...    ..6",			
			"9..    7..    ...",			
			"52.    ..9    .47",			
										"",
										"",
			"231    ...    ...",			
			"..6    .2.    1.9",			
			"...    ...    ...",			
	};
	
	static String[] s_initialValuesLeMondeHard2 = {
			
			"...    76.    ..8",			
			"9..    ...    ...",			
			"...    2.9    ...",			
										"",
										"",
			".5.    912    .46",			
			".9.    .4.    2.5",			
			"..6    .8.    .1.",			
										"",
										"",
			"...    .95    ..1",			
			".7.    ..6    ...",			
			"3.9    8..    4..",			
	};
	
	// https://puzzling.stackexchange.com/questions/37804/non-brute-force-sudoku
	static String[] s_initialValuesStackOverflow = {
			
			"3..    .72    596",			
			"...    4..    ..2",			
			"..7    ...    3.4",			
										"",
										"",
			"...    ...    .4.",			
			"...    ...    ...",			
			".9.    ...    ...",			
										"",
										"",
			"8.4    ...    2..",			
			"9..    ..7    ...",			
			"736    24.    ..9",			
	};
	
	static String[] s_initialValuesStackOverflow2 = {
			
			"3..    .72    596",			
			"1..    4..    ..2",			
			"..7    ...    3.4",			
										"",
										"",
			"...    ...    .4.",			
			"...    .8.    .5.",			
			".9.    ...    ...",			
										"",
										"",
			"8.4    ...    2..",			
			"9..    ..7    ...",			
			"736    24.    ..9",			
	};

	static String[] s_1 = {
			
			"...    .4.    .52",			
			"..2    1.5    8.7",			
			"...    ...    .4.",			
										"",
										"",
			"6.8    4..    .9.",			
			"3.5    ...    ..8",			
			"...    ..9    .1.",			
										"",
										"",
			"57.    .23    .8.",			
			"..9    ...    ...",			
			"..6    .17    ...",			
	};


	// https://www.youtube.com/watch?v=myy7ldfgTnQ
	static String[] s_times9636 = {
			
			"...    ..9    ...",			
			"...    .4.    ...",			
			"234    58.    ...",			
										"",
										"",
			"...    ...    ..1",			
			"76.    .9.    48.",			
			"39.    ...    5..",			
										"",
										"",
			".5.    ...    7..",			
			"..9    17.    8..",			
			"4..    93.    2..",			
	};

	// https://www.youtube.com/watch?v=4FlfjmmcjPs
	static String[] s_times9633 = {
			
			"...    ..7   35.",			
			"...    .2.    19.",			
			"...    ..1    .2.",			
										"",
										"",
			"..6    .5.    ..3",			
			".83    ...    67.",			
			"7..    .6.    4..",			
										"",
										"",
			".6.    3..    ...",			
			".92    .8.    ...",			
			".54    6..    ...",			
	};

	// https://www.youtube.com/watch?v=o3PQrNecoag
	// Needs combinations to get going ...
	static String[] s_hard = {
			
			"9..    ...    7..",			
			"..8    4.5    ...",			
			".5.    ..2    ..3",			
										"",
										"",
			"8..    .9.    ...",			
			"..4    ...    6..",			
			"...    .1.    ..2",			
										"",
										"",
			"5..    8..    .4.",			
			"...    7.9    8..",			
			"..2    ...    ..7",			
	};
	
	static String[] s_times9656 = {
			
			"...    ..2    .9.",			
			".9.    ...    .5.",			
			"...    13.    ..4",			
										"",
										"",
			"..3    ...    .7.",			
			"..6    ..4    9.5",			
			"2..    .7.    8..",			
										"",
										"",
			"...    .18    ..7",			
			"65.    7..    ..9",			
			"..7    .4.    28.",			
	};

	static String[] s_times9688 = {
			
			"...    ...    .6.",			
			"...    3..    28.",			
			"...    .15    ..9",			
										"",
										"",
			".5.    .7.    .1.",			
			"..1    4..    ..7",			
			"..4    ...    8.6",			
										"",
										"",
			".8.    ..2    .7.",			
			"34.    6..    9..",			
			"..5    .47    ...",			
	};

	static String[] s_empty = {
			
			"...    ...    ...",			
			"...    ...    ...",			
			"...    ...    ...",			
										"",
										"",
			"...    ...    ...",			
			"...    ...    ...",			
			"...    ...    ...",			
										"",
										"",
			"...    ...    ...",			
			"...    ...    ...",			
			"...    ...    ...",			
	};

	static String[] s_initialValues = s_times9688;
	
	public static Logger L = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static void main(String args[])
	{
		try {
			TheLogger.setup();
		} catch(IOException e) {
			System.err.println("Error setting up logger: " + e.getMessage());
			return;
		}
		
		L.setLevel(Level.ALL);
		
		List<String> initialGridValues = null;
		
		if(args.length > 0) {
			String puzzleFileName = args[0];
			System.out.println("Reading initial values from file " + puzzleFileName);
			initialGridValues = readPuzzleFile(puzzleFileName);
			if(initialGridValues == null) {
				System.err.println("Failed to read puzzle from file " + puzzleFileName);
			}
		}
		else {
			System.out.println("Using hard-coded initial values");
			initialGridValues = Arrays.asList(s_initialValues);
		}
		
		if(initialGridValues == null) return;

		L.info("New Puzzle run");
		System.out.println("Initial values:");
		System.out.println();
		for(String s : initialGridValues)
		{
			System.out.println("  " + s);
			L.info("Initial values:     " + s);
		}
		
		System.out.println();
		
		Puzzle puzzle = new Puzzle();		
		InitialGridStatus status = puzzle.assignInitialValues(initialGridValues);
		
		if(!status.m_isOK)
		{
			System.err.println("Error in initial grid: " + status.m_errorMessage);
			L.info("Error in initial grid: " + status.m_errorMessage);
		}
		else
		{
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

				changed = puzzle.m_solver.lookForNextAssignment(stepNumber);
				
				puzzle.m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay(), stepNumber);
				puzzle.m_solver.printGrid(new CellAssessment.CouldBeValueDisplay(), stepNumber);
				puzzle.m_solver.printCellSets(stepNumber);
				puzzle.m_solver.printGrid(new CellAssessment.AssignedValueDisplay(), stepNumber);

				Solver.Stats stats = puzzle.m_solver.getStats();
				complete = stats.m_complete;

				System.out.println("After step " + stepNumber + ": ");
				System.out.println("- " + stats.m_assignedCells + " assigned cells out of " + stats.m_cellCount + " (" + stats.m_initialAssignedCells + " givens)");
				System.out.println("- " + stats.m_unassignedCells + " unassigned cell" + ((stats.m_unassignedCells == 1) ? "" : "s"));
				System.out.println();
				
				if(complete)
				{
					System.out.println("Puzzle is complete");
					L.info("Puzzle completed");
					puzzle.m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
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
		}
		
		L.info("Puzzle run ended");
	}	

	
	static List<String> readPuzzleFile(String fileName) {
		
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

	public static List<Symbol> getListOfSymbols(int n)
	{
		// Need to check n ????
		List<Symbol> l = new ArrayList<>();
		for(Symbol cs : Symbol.values())
		{
			l.add(cs);
		}
		
		return l;
	}

	Grid m_grid;
	List<Symbol> m_symbolsList;
	Solver m_solver;
	
	Puzzle()
	{
		m_grid = new Grid();
		m_symbolsList = getListOfSymbols(9);
		m_solver = new Solver(m_grid, m_symbolsList);
		
		m_solver.printGrid(new CellAssessment.CellNumberDisplayer());
		m_solver.printGrid(new CellAssessment.BoxNumberDisplayer());
		m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay());
		m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
		m_solver.printCellSets();
	}
	
	InitialGridStatus assignInitialValues(List<String> inputValueRows)
	{
		InitialGridStatus status = new InitialGridStatus();
		List<String> valueRows = removeBlankLines(inputValueRows);
		
		// Should be 9 rows, each with 9 characters, ignored spaces.
		if(valueRows.size() != 9)
		{
			status.setError("Unexpected number of rows: " + valueRows.size());
		}
		else
		{
			for(int rowNumber = 0; rowNumber < valueRows.size(); rowNumber ++)
			{
				String rowString = valueRows.get(rowNumber);
				String despacedRowString = rowString.replaceAll("\\s+", "");
				if(despacedRowString.length() != 9)
				{
					status.setError("Unexpected number of columns: " + despacedRowString.length() + " : [" + rowString + "]");
				}
				else
				{
					for(int columnNumber = 0; columnNumber < despacedRowString.length(); columnNumber ++)
					{
						char c = despacedRowString.charAt(columnNumber);
						if(c == '.')
						{
							// Ignore
						}
						else
						{
							Symbol symbol = Symbol.toSymbol(c);
							if(symbol != null)
							{
								CellAssignmentStatus assignmentStatus = m_solver.applyGivenValueToCell(rowNumber, columnNumber, symbol);
								if(assignmentStatus != CellAssignmentStatus.CanBeAssigned)
								{
									status.setError("Unable to assign " + symbol.getGridRepresentation() + " to row " + rowNumber + " column " + columnNumber + ": " + assignmentStatus.toString());
								}
							}
							else
							{
								status.setError("Unknown symbol: " + c);
							}
						}
					}
				}
			}
		}
				
		// Apply to the grid
		// Check for clashes in rows/columns/boxes
		
		if(status.m_isOK)
		{
			m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay(), 0);
			m_solver.printGrid(new CellAssessment.CouldBeValueDisplay(), 0);
			m_solver.printCellSets();
			m_solver.printGrid(new CellAssessment.AssignedValueDisplay(), 0);
		}

		return status;
	}
	
	private static List<String> removeBlankLines(List<String> l)
	{
		List<String> lOut = new ArrayList<>();
		for(String s : l)
		{
			if(s.trim().length() > 0)
			{
				lOut.add(s);
			}
		}
		
		return lOut;
	}

		
	class InitialGridStatus
	{
		boolean m_isOK;
		String m_errorMessage;
		
		InitialGridStatus()
		{
			m_isOK = true;
			m_errorMessage = "";
		}
		
		void setError(String message)
		{
			m_isOK = false;
			m_errorMessage = message;
		}
	}
		
}
