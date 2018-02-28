package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grid.Cell;
import grid.Grid;
import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Puzzle;
import puzzle.Symbol;

public class Solver {

	Grid m_grid;
	
	List<Symbol> m_lSymbols;
	
	List<RowAssessment> m_lRows;
	List<ColumnAssessment> m_lColumns;
	List<BoxAssessment> m_lBoxes;	
	List<CellAssessment> m_lCells;
	List<CellSetAssessment> m_lCellSets;
	
	HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	
	public Solver(Grid grid, List<Symbol> lSymbols) {
		m_grid = grid;
		m_lSymbols = new ArrayList<>(lSymbols);

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

	public void printCellSets() {
		printCellSets(-1);
	}
	
	public void printCellSets(int stepNumber) {
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


	public void printGrid(CellContentDisplayer ccd) { printGrid(ccd, -1); }
	
	public void printGrid(CellContentDisplayer ccd, int stepNumber) {
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

	public CellAssignmentStatus applyGivenValueToCell(int rowNumber, int columnNumber, Symbol symbol)
	{
		Puzzle.L.info("Applying given value : " + symbol.getGridRepresentation() + " to cell in row " + rowNumber + ", column " + columnNumber);
		int cellNumber = Grid.getCellNumberFromGridPosition(rowNumber, columnNumber);
		CellAssessment cell = m_lCells.get(cellNumber);
//		Cell cell = m_aCells[rowNumber][columnNumber];
		Assignment a = new Assignment(cell.m_cell, symbol, AssignmentMethod.Given, "", 0);
		CellAssignmentStatus status = cell.setAsAssigned(a);
		return status;
	}

	CellAssessment getCellAssessmentForCell(Cell cell) {
		return m_cellAssessmentsMap.get(cell);
	}
	
	public boolean lookForNextAssignment(int stepNumber)
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
	
	public Stats getStats()
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

	public class Stats
	{
		public boolean m_complete;
		public int m_cellCount;
		public int m_initialAssignedCells;
		public int m_assignedCells;
		public int m_unassignedCells;
		
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
