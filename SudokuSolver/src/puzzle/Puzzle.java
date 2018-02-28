package puzzle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import grid.BoxAssessment;
import grid.Cell;
import grid.CellAssessment;
import grid.CellAssignmentStatus;
import grid.CellContentDisplayer;
import grid.CellSetAssessment;
import grid.ColumnAssessment;
import grid.Grid;
import grid.RowAssessment;
import grid.SymbolRestriction;
import grid.SymbolSetRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

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
		
		System.out.println("Initial values:");
		System.out.println();
		L.info("New Puzzle run");
		for(String s : s_initialValues)
		{
			System.out.println("  " + s);
			L.info("Initial values:     " + s);
		}
		
		System.out.println();
		
		Puzzle puzzle = new Puzzle();		
		InitialGridStatus status = puzzle.assignInitialValues(s_initialValues);
		
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
	
	Grid m_grid;
	Solver m_solver;
	
	Puzzle()
	{
		m_grid = new Grid();		
		m_solver = new Solver(m_grid);
		
		m_solver.printGrid(new CellAssessment.CellNumberDisplayer());
		m_solver.printGrid(new CellAssessment.BoxNumberDisplayer());
		m_solver.printGrid(new CellAssessment.CouldBeValueCountDisplay());
		m_solver.printGrid(new CellAssessment.AssignedValueDisplay());
		m_solver.printCellSets();
	}
	
	InitialGridStatus assignInitialValues(String[] inputValueRows)
	{
		InitialGridStatus status = new InitialGridStatus();
		String valueRows[] = removeBlankLines(inputValueRows);
		
		// Should be 9 rows, each with 9 characters, ignored spaces.
		if(valueRows.length != 9)
		{
			status.setError("Unexpected number of rows: " + valueRows.length);
		}
		else
		{
			for(int rowNumber = 0; rowNumber < valueRows.length; rowNumber ++)
			{
				String rowString = valueRows[rowNumber];
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
	
	private static String[] removeBlankLines(String a[])
	{
		int n=0;
		for(String s : a)
		{
			if(s.trim().length() > 0) n++;
		}
		
		String[] realRows = new String[n];
		n = 0;
		for(String s : a)
		{
			if(s.trim().length() > 0)
			{
				realRows[n++] = s;
			}
		}
		
		return realRows;
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

class Solver {

	Grid m_grid;
	
	List<Symbol> m_lSymbols;
	
	List<RowAssessment> m_lRows;
	List<ColumnAssessment> m_lColumns;
	List<BoxAssessment> m_lBoxes;	
	List<CellAssessment> m_lCells;
	List<CellSetAssessment> m_lCellSets;
	
	HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	
	Solver(Grid grid) {
		m_grid = grid;
		m_lSymbols = new ArrayList<>(m_grid.m_lSymbols);

		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellSets = new ArrayList<>();
		m_lCells = new ArrayList<>();
		m_cellAssessmentsMap = new HashMap<>();
		
		for(int rowNum = 0; rowNum < m_grid.m_lRows.size(); rowNum++)
		{
			m_lRows.add(new RowAssessment(m_grid.m_lRows.get(rowNum), m_lSymbols));
		}
		
		for(int columnNum = 0; columnNum < m_grid.m_lColumns.size(); columnNum++)
		{
			m_lColumns.add(new ColumnAssessment(m_grid.m_lColumns.get(columnNum), m_lSymbols));
		}

		for(int boxNum = 0; boxNum < m_grid.m_lBoxes.size(); boxNum++)
		{
			m_lBoxes.add(new BoxAssessment(m_grid.m_lBoxes.get(boxNum), m_lSymbols));
		}
		
		m_lCellSets = new ArrayList<>(m_lRows);
		m_lCellSets.addAll(m_lColumns);
		m_lCellSets.addAll(m_lBoxes);
		
		for(int rowNum = 0; rowNum < m_lRows.size(); rowNum++)
		{
			RowAssessment row = m_lRows.get(rowNum);
			for(int columnNum = 0; columnNum < m_lColumns.size(); columnNum++)
			{
				ColumnAssessment column = m_lColumns.get(columnNum);

				int boxNum = Grid.getBoxNumberFromGridPosition(rowNum, columnNum);		// ????
				BoxAssessment box = m_lBoxes.get(boxNum);											// ????
				int cellNumber = Grid.getCellNumberFromGridPosition(rowNum, columnNum);
				Cell cell = m_grid.m_lCells.get(cellNumber);
				CellAssessment cellAssessment = new CellAssessment(cell, row, column, box, m_lSymbols);				
				m_lCells.add(cellAssessment);
				m_cellAssessmentsMap.put(cell, cellAssessment);
				row.addCell(cellAssessment);
				column.addCell(cellAssessment);
				box.addCell(cellAssessment);
			}			
		}		

	}

	private static String s_divider = "-----------------------------------";

	void printCellSets() {
		printCellSets(-1);
	}
	
	void printCellSets(int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append("Cell sets "  + stepInfo);
		sb1.append("\r\n");
		
		for(CellSetAssessment cellset : m_lCellSets)
		{
			sb1.append(cellset.m_cellSet.getRepresentation() + " : " + cellset.getSymbolAssignmentSummary());
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}


	void printGrid(CellContentDisplayer ccd) { printGrid(ccd, -1); }
	
	void printGrid(CellContentDisplayer ccd, int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		int currentHorizontalBoxNumber = -1;
		int currentVerticalBoxNumber = -1;
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append(ccd.getHeading() + stepInfo);
		sb1.append("\r\n");
		
		for(int rowNumber = 0; rowNumber < m_lRows.size(); rowNumber++)
		{
			int boxNumber = Grid.getBoxNumberFromGridPosition(rowNumber, 0);
			if(boxNumber != currentVerticalBoxNumber)
			{
				sb1.append("\r\n\r\n");
				currentVerticalBoxNumber = boxNumber; 
			}

			for(int columnNumber = 0; columnNumber < m_lColumns.size(); columnNumber++)
			{
				boxNumber = Grid.getBoxNumberFromGridPosition(rowNumber, columnNumber);
				if(boxNumber != currentHorizontalBoxNumber)
				{
					sb1.append("    ");
					currentHorizontalBoxNumber = boxNumber;
				}

				int cellNumber = Grid.getCellNumberFromGridPosition(rowNumber, columnNumber);
				CellAssessment cell = m_lCells.get(cellNumber);
				boolean highlight = (cell.m_cell.isAssigned() && (cell.m_cell.getAssignment().getStepNumber() == stepNumber));
				String contents = ccd.getContent(cell, highlight);
				sb1.append(" " + contents + " ");					
			}
			
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}

	CellAssignmentStatus applyGivenValueToCell(int rowNumber, int columnNumber, Symbol symbol)
	{
		Puzzle.L.info("Applying given value : " + symbol.getGridRepresentation() + " to cell in row " + rowNumber + ", column " + columnNumber);
		int cellNumber = Grid.getCellNumberFromGridPosition(rowNumber, columnNumber);
		CellAssessment cell = m_lCells.get(cellNumber);
//		Cell cell = m_aCells[rowNumber][columnNumber];
		Assignment a = new Assignment(cell.m_cell, symbol, AssignmentMethod.Given, 0);
		CellAssignmentStatus status = cell.setAsAssigned(a);
		return status;
	}

	CellAssessment getCellAssessmentForCell(Cell cell) {
		return m_cellAssessmentsMap.get(cell);
	}
	
	boolean lookForNextAssignment(int stepNumber)
	{
		boolean changedState = false;
		
		Puzzle.L.info("Starting assignment step: " + stepNumber + " ..");
		
		if(!changedState)
		{
			// Look through unassigned cell for cases where only one symbol is a possible assignment.
			for(CellAssessment cell : m_lCells)
			{
				if(!cell.m_cell.isAssigned())
				{
					Assignment a = cell.checkForAssignableSymbol(stepNumber);
					if(a != null)
					{
						String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + cell.m_cell.getColumnAndRowLocationString();
						Puzzle.L.info(s);
						System.out.println(s);
						System.out.println();
						getCellAssessmentForCell(a.getCell()).setAsAssigned(a);
						changedState = true;
						break;
					}
				}
			}			
		}
		
		if(!changedState)
		{
			// Look through each row, column, box for an unassigned symbol which can only go in one cell
			for(CellSetAssessment set : m_lCellSets)
			{
				Assignment a = set.checkForAssignableSymbol(stepNumber);
				if(a != null)
				{
					String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + a.getCell().getColumnAndRowLocationString() + " from cell set " + set.m_cellSet.getRepresentation();
					Puzzle.L.info(s);
					System.out.println(s);
					System.out.println();
					getCellAssessmentForCell(a.getCell()).setAsAssigned(a);
					changedState = true;
					break;
				}
			}
		}
		
		if(!changedState)
		{
			// Look through each box to see where a particular unresolved symbol can only appear in a specific row or column of the box.
			// Where this arises, we can rule-out the symbol from the other cells in the row or column which are not in the box.
			int stateChanges = 0;
			for(BoxAssessment box : m_lBoxes)
			{
				List<SymbolRestriction> lRestrictions = box.findRestrictedSymbols();
				if(lRestrictions != null)
				{
					for(SymbolRestriction restriction : lRestrictions)
					{
						boolean causedStateChange = restriction.m_rowOrColumn.ruleOutSymbolOutsideBox(restriction);
						if(causedStateChange)
						{
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState)
		{
			// Look through each column box to see where a particular unresolved symbol can only appear in a specific box.
			// Where this arises, we can rule-out the symbol from the other cells in the box which are not in the column.
			int stateChanges = 0;
			for(ColumnAssessment column : m_lColumns)
			{
				List<SymbolRestriction> lRestrictions = column.findRestrictedSymbols();
				if(lRestrictions != null)
				{
					for(SymbolRestriction restriction : lRestrictions)
					{
						boolean causedStateChange = restriction.m_box.ruleOutSymbolOutsideRowOrColumn(restriction);
						if(causedStateChange)
						{
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState)
		{
			// And the same again for rows ...
			int stateChanges = 0;
			for(RowAssessment row : m_lRows)
			{
				List<SymbolRestriction> lRestrictions = row.findRestrictedSymbols();
				if(lRestrictions != null)
				{
					for(SymbolRestriction restriction : lRestrictions)
					{
						boolean causedStateChange = restriction.m_box.ruleOutSymbolOutsideRowOrColumn(restriction);
						if(causedStateChange)
						{
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState)
		{
			// Where n symbols in a row/column/box can only be assigned to the same n cells, then these cells can't be assigned to any other symbols.
			int stateChanges = 0;
			for(CellSetAssessment set : m_lCellSets)
			{
				List<SymbolSetRestriction> lRestrictedSymbolSets = set.findRestrictedSymbolSets();
				if(lRestrictedSymbolSets != null)
				{
					for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets)
					{						
						for(CellAssessment cell : symbolSetRestriction.m_lCells)
						{
							boolean causedStateChange = cell.ruleOutAllBut(symbolSetRestriction.m_lSymbols);
							if(causedStateChange)
							{
								stateChanges++;
							}
						}
					}

					for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets)
					{
						for(Symbol symbol : symbolSetRestriction.m_lSymbols)
						{
							boolean causedStateChange = symbolSetRestriction.m_cellSet.ruleOutAllCellsBut(symbol, symbolSetRestriction.m_lCells);
							if(causedStateChange)
							{
								stateChanges++;
							}
						}
						
						List<CellSetAssessment> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
						for(CellSetAssessment cset : lAffectedCellSets)
						{
							for(CellAssessment cell : symbolSetRestriction.m_lCells)
							{
								boolean causedStateChange = cset.ruleOutCellFromOtherSymbols(cell, symbolSetRestriction.m_lSymbols);
								if(causedStateChange)
								{
									stateChanges++;
								}
							}
						}
					}

				
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		return changedState;
	}
	
	boolean checkForCompletion()
	{
		boolean isComplete = true;
		
		// All cell-sets are finished
		for(CellSetAssessment set : m_lCellSets)
		{
			if(!set.isComplete())
			{
				isComplete = false;
			}
		}
		
		// All cells are assigned
		for(Cell cell : m_grid.m_lCells)
		{
			if(!cell.isAssigned())
			{
				isComplete = false;
			}
		}
		
		return isComplete;
	}
	
	Stats getStats()
	{
		Stats stats = new Stats();
		stats.m_complete = checkForCompletion();
		stats.m_cellCount = m_grid.m_lCells.size();
		stats.m_initialAssignedCells = 0;
		stats.m_assignedCells = 0;
		stats.m_unassignedCells = 0;

		for(Cell cell : m_grid.m_lCells)
		{
			if(cell.isAssigned())
			{
				stats.m_assignedCells++;
				if(cell.getAssignment().getMethod() == AssignmentMethod.Given)
				{
					stats.m_initialAssignedCells++;
				}
			}
			else
			{
				stats.m_unassignedCells++;
			}
		}
		return stats;
	}

	class Stats
	{
		boolean m_complete;
		int m_cellCount;
		int m_initialAssignedCells;
		int m_assignedCells;
		int m_unassignedCells;
		
		Stats()
		{
			m_complete = false;
			m_cellCount = -1;
			m_initialAssignedCells = -1;
			m_assignedCells = -1;
			m_unassignedCells = -1;
		}
	}

}