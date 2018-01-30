import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Puzzle {

	
	static String[] xs_initialValues = {
			
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
	
	static String[] s_initialValues = {
			
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
				
				if(complete)
				{
					System.out.println("Puzzle completed");
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
					System.out.println("Change identified, continuing puzzle ..");
					L.info("Change identified, continuing puzzle ..");															
				}
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
				a.m_cell.setAsAssigned(AssignmentMethod.AssignedSymbolToCellSet, a.m_symbol);
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
