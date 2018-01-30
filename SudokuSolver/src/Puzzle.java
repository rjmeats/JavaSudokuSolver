import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	static String[] s_initialValues = s_initialValuesLeMondeHard;
	
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
				changed = puzzle.lookForNextAssignment(stepNumber);
				
				Stats stats = puzzle.getStats();
				complete = stats.m_complete;

				System.out.println("After step " + stepNumber + ": ");
				System.out.println("- " + stats.m_assignedCells + " assigned cells out of " + stats.m_cellCount + " (" + stats.m_initialAssignedCells + " givens)");
				System.out.println("- " + stats.m_unassignedCells + " unassigned cell" + ((stats.m_unassignedCells == 1) ? "" : "s"));
				System.out.println();
				
				if(complete)
				{
					System.out.println("Puzzle is complete");
					L.info("Puzzle completed");
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
	
	Puzzle()
	{
		m_grid = new Grid();
		
		m_grid.printGrid(new Cell.CellNumberDisplayer());
		m_grid.printGrid(new Cell.BoxNumberDisplayer());
		m_grid.printGrid(new Cell.CouldBeValueCountDisplay());
		m_grid.printGrid(new Cell.AssignedValueDisplay());
		m_grid.printCellSets();
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
							CellSymbol symbol = CellSymbol.toCellSymbol(c);
							if(symbol != null)
							{
								CellAssignmentStatus assignmentStatus = m_grid.applyGivenValueToCell(rowNumber, columnNumber, symbol);
								if(assignmentStatus != CellAssignmentStatus.CanBeAssigned)
								{
									status.setError("Unable to assign " + symbol.getRepresentation() + " to row " + rowNumber + " column " + columnNumber + ": " + assignmentStatus.toString());
								}
							}
							else
							{
								status.setError("Unexpected symbol: " + c);
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
			m_grid.printGrid(new Cell.CouldBeValueCountDisplay(), 0);
			m_grid.printGrid(new Cell.CouldBeValueDisplay(), 0);
			m_grid.printGrid(new Cell.AssignedValueDisplay(), 0);
			m_grid.printCellSets();
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

	boolean lookForNextAssignment(int assignmentStep)
	{
		boolean changedState = false;
		
		System.out.println("======================================================================");
		System.out.println("Assignment step: " + assignmentStep);
		Puzzle.L.info("Starting assignment step: " + assignmentStep + " ..");
		
		// Look through each row, column, box for an unassigned symbol which can only go in one cell
		Assignment a = null;
		for(CellSet set : m_grid.m_lCellSets)
		{
			a = set.checkForAssignableSymbol();
			if(a != null)
			{
				String s = "Assigned symbol " + a.m_symbol.getRepresentation() + " to cell " + a.m_cell.m_cellNumber + " from cell set " + set.getRepresentation();
				Puzzle.L.info(s);
				System.out.println(s);
				System.out.println();
				a.m_cell.setAsAssigned(AssignmentMethod.AssignedSymbolToCellSet, a.m_symbol, assignmentStep);
				changedState = true;

				m_grid.printGrid(new Cell.CouldBeValueCountDisplay(), assignmentStep);
				m_grid.printGrid(new Cell.CouldBeValueDisplay(), assignmentStep);
				m_grid.printGrid(new Cell.AssignedValueDisplay(), assignmentStep);
				m_grid.printCellSets(assignmentStep);
				
				break;
			}
		}
		
		return changedState;
	}
	
	boolean checkForCompletion()
	{
		boolean isComplete = true;
		
		// All cell-sets are finished
		for(CellSet set : m_grid.m_lCellSets)
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
				if(cell.m_value.m_method == AssignmentMethod.Given)
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
