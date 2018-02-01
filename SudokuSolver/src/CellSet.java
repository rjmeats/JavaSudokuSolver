import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public abstract class CellSet {

	List<Cell> m_lCells;
	HashMap<Symbol, Assignment> m_assignedSymbols;
	HashMap<Symbol, List<Cell>> m_couldBeCellsForSymbol;
		
	public CellSet(List<Symbol> lSymbols) {
		m_lCells = new ArrayList<>();
		m_assignedSymbols = new HashMap<>();
		
		m_couldBeCellsForSymbol = new HashMap<>();		
		for(Symbol symbol : lSymbols)
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

	boolean symbolAlreadyAssigned(Symbol symbol)
	{
		return m_assignedSymbols.containsKey(symbol);
	}
	
	// A particular symbol has been assigned to a cell, so mark it as ruled-out for other cells in this set.
	void markAsAssigned(Assignment assignment)
	{
		Symbol symbol = assignment.getSymbol();
		Cell assignmentCell = assignment.getCell();
		
		Puzzle.L.info(".. marking symbol " + symbol.toString() + " as assigned to cell " + assignmentCell.getCellNumber() + " in cellset " + getRepresentation());
		
		// Add to the list of symbols in this set which are now assigned.
		m_assignedSymbols.put(symbol, assignment);

		List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.clear();
		lCellsForThisSymbol.add(assignmentCell);

		// And go through all the other cells sharing a cell set with this set, ruling out this symbol for their use.
		assignmentCell.getRow().ruleOutSymbolFromOtherCells(symbol, assignmentCell);
		assignmentCell.getColumn().ruleOutSymbolFromOtherCells(symbol, assignmentCell);
		assignmentCell.getBox().ruleOutSymbolFromOtherCells(symbol, assignmentCell);

		// And go through all the symbols for other cell-sets sharing a cell set with this set, ruling out this symbol for use in their other sets.
		assignmentCell.getRow().ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
		assignmentCell.getColumn().ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
		assignmentCell.getBox().ruleOutCouldBeCellsForSymbol(symbol, assignmentCell);
}

	void ruleOutSymbolFromOtherCells(Symbol symbol, Cell assignmentCell)
	{
		for(Cell otherCell : m_lCells)
		{
			if(otherCell != assignmentCell)
			{
				Puzzle.L.info(".. ruling out " + symbol.toString() + " for cell " + otherCell.getCellNumber() + " in cell-set " + getRepresentation());				
				otherCell.ruleOut(symbol);
				if(this != otherCell.getRow()) otherCell.getRow().ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.getColumn()) otherCell.getColumn().ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.getBox()) otherCell.getBox().ruleOutSymbolForCell(symbol, otherCell);
			}
		}
		
	}

	void ruleOutSymbolForCell(Symbol symbol, Cell cell)
	{
		List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.remove(cell);
		Puzzle.L.info(".. ruling out " + symbol.toString() + " for cell " + cell.getCellNumber() + " in cell-set " + getRepresentation());
  		Puzzle.L.info(".. cell-could-be list for symbol = " + cellListToString(lCellsForThisSymbol));
	}

	void ruleOutCouldBeCellsForSymbol(Symbol symbol, Cell assignmentCell)
	{
		for(Symbol otherSymbol : m_couldBeCellsForSymbol.keySet())
		{
			if(otherSymbol != symbol)
			{				
				List<Cell> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(otherSymbol);
				Puzzle.L.info(".. ruling out cell " + assignmentCell.getCellNumber() + " for symbol " + otherSymbol.toString() + " in cell-set " + getRepresentation());				
				lCellsForThisSymbol.remove(assignmentCell);
				Puzzle.L.info(".. cell-could-be list for symbol = " + cellListToString(lCellsForThisSymbol));
			}
		}		
	}
	
	Assignment checkForAssignableSymbol(int stepNumber)
	{
		Assignment assignableCell = null;
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			if(!m_assignedSymbols.containsKey(symbol))
			{
				List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
				if(lCells.size() == 1)
				{
					assignableCell = new Assignment(lCells.get(0), symbol, AssignmentMethod.AssignedSymbolToCellSet, stepNumber);
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
			sb.append(cell.getCellNumber()).append(" ");
		}
		return sb.toString().trim();
	}

	String getSymbolAssignmentSummary()
	{
		StringBuilder sb = new StringBuilder();
		List<Symbol> lSymbols = new ArrayList<>(m_couldBeCellsForSymbol.keySet());
		Collections.sort(lSymbols);
				
		for(Symbol symbol : lSymbols)
		{
			List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
			String cellListString = cellListToString(lCells);
			
			sb.append(symbol.getGridRepresentation() + ":[" + cellListString + "] ");
		}
		
		return sb.toString().trim();
	}
	
}
