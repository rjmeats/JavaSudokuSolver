import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public abstract class CellSet {

	List<Cell> m_lCells;
	HashMap<CellSymbol, Cell> m_assignedSymbols;
	HashMap<CellSymbol, List<Cell>> m_couldBeCellsForSymbol;
		
	public CellSet(List<CellSymbol> lSymbols) {
		m_lCells = new ArrayList<>();
		m_assignedSymbols = new HashMap<>();
		
		m_couldBeCellsForSymbol = new HashMap<>();		
		for(CellSymbol symbol : lSymbols)
		{
			m_couldBeCellsForSymbol.put(symbol, new ArrayList<Cell>());
		}
	}

	public abstract String getRepresentation();

	void addCell(Cell cell)	{
		m_lCells.add(cell);
		for(List<Cell> lCells : m_couldBeCellsForSymbol.values())
		{
			lCells.add(cell);
		}
	}

	boolean symbolAlreadyAssigned(CellSymbol symbol)
	{
		return m_assignedSymbols.containsKey(symbol);
	}
	
	// A particular symbol has been assigned to a cell, so mark it as ruled-out for other cells in this set.
	void markAsAssigned(CellSymbol symbol, Cell assignmentCell)
	{
		Puzzle.L.info(".. marking symbol " + symbol.getRepresentation() + " as assigned to cell " + assignmentCell.m_cellNumber + " in cellset " + getRepresentation());
		// Add to the list of symbols in this set which are now assigned.
		m_assignedSymbols.put(symbol, assignmentCell);

		List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.clear();
		lCellsForThisSymbol.add(assignmentCell);

		// And go through all the other cells sharing a cell set with this set, ruling out this symbol for their use.
		assignmentCell.m_row.ruleOutSymbolFromOtherCells(symbol, assignmentCell);
		assignmentCell.m_column.ruleOutSymbolFromOtherCells(symbol, assignmentCell);
		assignmentCell.m_box.ruleOutSymbolFromOtherCells(symbol, assignmentCell);

		// And go through all the symbols for other cell-sets sharing a cell set with this set, ruling out this symbol for use in their other sets.
		assignmentCell.m_row.ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
		assignmentCell.m_column.ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
		assignmentCell.m_box.ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
}

	void ruleOutSymbolFromOtherCells(CellSymbol symbol, Cell assignmentCell)
	{
		for(Cell otherCell : m_lCells)
		{
			if(otherCell != assignmentCell)
			{
				Puzzle.L.info(".. ruling out " + symbol.getRepresentation() + " for cell " + otherCell.m_cellNumber + " in cell-set " + getRepresentation());				
				otherCell.ruleOut(symbol);
				if(this != otherCell.m_row) otherCell.m_row.ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.m_column) otherCell.m_column.ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.m_box) otherCell.m_box.ruleOutSymbolForCell(symbol, otherCell);
			}
		}
		
	}

	void ruleOutSymbolForCell(CellSymbol symbol, Cell cell)
	{
		List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.remove(cell);
		Puzzle.L.info(".. ruling out " + symbol.getRepresentation() + " for cell " + cell.m_cellNumber + " in cell-set " + getRepresentation());
  		Puzzle.L.info(".. cell-could-be list for symbol = " + cellListToString(lCellsForThisSymbol));
	}

	void ruleOutCouldBeCellsForSymbol(CellSymbol symbol, Cell assignmentCell)
	{
		for(CellSymbol otherSymbol : m_couldBeCellsForSymbol.keySet())
		{
			if(otherSymbol != symbol)
			{				
				List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(otherSymbol);
				Puzzle.L.info(".. ruling out cell " + assignmentCell.m_cellNumber + " for symbol " + otherSymbol.getRepresentation() + " in cell-set " + getRepresentation());				
				lCellsForThisSymbol.remove(assignmentCell);
				Puzzle.L.info(".. cell-could-be list for symbol = " + cellListToString(lCellsForThisSymbol));
			}
		}		
	}
	
	Assignment checkForAssignableSymbol()
	{
		Assignment assignableCell = null;
		for(CellSymbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			if(!m_assignedSymbols.containsKey(symbol))
			{
				List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
				if(lCells.size() == 1)
				{
					assignableCell = new Assignment(lCells.get(0), symbol);
					break;
				}
			}
		}
		
		return assignableCell;
	}

	boolean isComplete()
	{
		return m_assignedSymbols.size() == m_lCells.size();
	}
	
	
	private static String cellListToString(List<Cell> l)
	{
		StringBuilder sb = new StringBuilder();
		Collections.sort(l, new Cell.SortByCellNumber());
		for(Cell cell: l)
		{
			sb.append(cell.m_cellNumber).append(" ");
		}
		return sb.toString().trim();
	}

	String getSymbolAssignmentSummary()
	{
		StringBuilder sb = new StringBuilder();
		List<CellSymbol> lSymbols = new ArrayList<>(m_couldBeCellsForSymbol.keySet());
		Collections.sort(lSymbols);
				
		for(CellSymbol symbol : lSymbols)
		{
			List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
			String cellListString = cellListToString(lCells);
			
			sb.append(symbol.getRepresentation() + ":[" + cellListString + "] ");
		}
		
		return sb.toString().trim();
	}
	
}
