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
	
	boolean containsCell(Cell cell)
	{
		return m_lCells.contains(cell);	
	}
	
	boolean ruleOutSymbolOutsideBox(SymbolRestriction restriction)
	{
		boolean changedState = false;
		// For cells not in the restriction box, rule out the symbol.
		for(Cell cell : m_lCells)
		{
			if(!restriction.m_box.containsCell(cell))
			{
				if(!cell.isRuledOut(restriction.m_symbol))
				{
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getColumnAndRowLocationString());				
					cell.ruleOut(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}
	
	boolean ruleOutSymbolOutsideRowOrColumn(SymbolRestriction restriction)
	{
		boolean changedState = false;
		// For cells not in the restriction row/column, rule out the symbol.
		for(Cell cell : m_lCells)
		{
			if(!restriction.m_rowOrColumn.containsCell(cell))
			{
				if(!cell.isRuledOut(restriction.m_symbol))
				{
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getColumnAndRowLocationString());				
					cell.ruleOut(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}
	
	
	List<SymbolPairRestriction> findRestrictedSymbolPairs()
	{
		List<SymbolPairRestriction> l = new ArrayList<>();
		
		// Go through each symbol. If it has two possible cells, see whether any other symbol can only be assigned to the
		// same two cells.
		
		for(Symbol symbol1 : m_couldBeCellsForSymbol.keySet())
		{
			List<Cell> lCells1 = m_couldBeCellsForSymbol.get(symbol1);
			if(lCells1.size() == 2)
			{
				for(Symbol symbol2 : m_couldBeCellsForSymbol.keySet())
				{
					if(symbol1 != symbol2)
					{
						List<Cell> lCells2 = m_couldBeCellsForSymbol.get(symbol2);
						if(lCells2.size() == 2)
						{
							System.err.println("Comparing symbol lists for " + symbol1.toString() + " and " + symbol2.toString() + " for set " + getRepresentation());							
							boolean same = compareCellLists(lCells1, lCells2);
							if(same)
							{
								System.err.println(".. lists are the same");
								if(symbol1.ordinal() < symbol2.ordinal())
								{
									System.err.println(".. keeping this one");
									SymbolPairRestriction restriction = new SymbolPairRestriction();
									restriction.m_cellSet = this;
									restriction.m_lCells = lCells1;
									restriction.m_lSymbols = new ArrayList<>();
									restriction.m_lSymbols.add(symbol1);
									restriction.m_lSymbols.add(symbol2);
									l.add(restriction);
								}
							}
						}						
					}
				}				
			}
		}

		
		// And do the same from a cell-based perspective		
		for(Cell cell1 : m_lCells)
		{
			List<Symbol> lSymbols1 = cell1.getCouldBeSymbolsList();
			if(lSymbols1.size() == 2)
			{
				for(Cell cell2 : m_lCells)
				{
					if(cell1 != cell2)
					{
						List<Symbol> lSymbols2 = cell2.getCouldBeSymbolsList();
						if(lSymbols2.size() == 2)
						{
							System.err.println("Comparing cell lists for " + cell1.getColumnAndRowLocationString() + " and " + cell2.getColumnAndRowLocationString() + " for set " + getRepresentation());							
							boolean same = compareSymbolLists(lSymbols1, lSymbols2);
							if(same)
							{
								System.err.println(".. lists are the same");
								if(cell1.getCellNumber() < cell2.getCellNumber())
								{
									System.err.println(".. keeping this one");
									SymbolPairRestriction restriction = new SymbolPairRestriction();
									restriction.m_cellSet = this;
									restriction.m_lSymbols = lSymbols1;
									restriction.m_lCells = new ArrayList<>();
									restriction.m_lCells.add(cell1);
									restriction.m_lCells.add(cell2);
									l.add(restriction);
								}
							}
						}						
					}
				}				
			}
		}

		return l;
	}
	
	boolean ruleOutAllCellsBut(Symbol symbol, List<Cell> lCells)
	{		
		
if(this instanceof Box && ((Box)this).getBoxNumber() == 8)
{
	int n = 199;
}
		boolean changed = false;
		
		List<Cell> lUnwantedCells = new ArrayList<>();
		List<Cell> lCouldBeCellsForSymbol = m_couldBeCellsForSymbol.get(symbol);
		for(Cell couldBeCell : lCouldBeCellsForSymbol)
		{
			if(!lCells.contains(couldBeCell))
			{
				lUnwantedCells.add(couldBeCell);
			}
		}
		
		for(Cell unwantedCell : lUnwantedCells)
		{
			if(lCouldBeCellsForSymbol.contains(unwantedCell))
			{
				lCouldBeCellsForSymbol.remove(unwantedCell);
				changed = true;
			}
		}
		
		return changed;
	}

	boolean ruleOutCellFromOtherSymbols(Cell cell, List<Symbol> lSymbols)
	{
		boolean changed = false;
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			if(!lSymbols.contains(symbol))
			{
				// Can't be assigned to this cell
				List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
				if(lCells.contains(cell))
				{
					lCells.remove(cell);
					System.err.println("Also remove cell " + cell.toString() + " from cell for symbol " + symbol.toString());					
					changed = true;
				}
			}
		}
		return changed;
	}

	
	
	static boolean compareCellLists(List<Cell> lCells1, List<Cell> lCells2)
	{
		boolean same = true;

		if(lCells1.size() != lCells2.size()) return false;
		
		List<Cell> l1 = new ArrayList<>(lCells1);
		List<Cell> l2 = new ArrayList<>(lCells2);
		
		Collections.sort(l1, new Cell.SortByCellNumber());
		Collections.sort(l2, new Cell.SortByCellNumber());

		for(int n=0; n < l1.size(); n++)
		{
			if(l1.get(n) != l2.get(n))
			{
				same = false;
				break;
			}
		}
		
		return same;
	}

	static boolean compareSymbolLists(List<Symbol> lSymbols1, List<Symbol> lSymbols2)
	{
		boolean same = true;

		if(lSymbols1.size() != lSymbols2.size()) return false;
		
		List<Symbol> l1 = new ArrayList<>(lSymbols1);
		List<Symbol> l2 = new ArrayList<>(lSymbols2);
		
		Collections.sort(l1, new Symbol.SortBySymbol());
		Collections.sort(l2, new Symbol.SortBySymbol());

		for(int n=0; n < l1.size(); n++)
		{
			if(l1.get(n) != l2.get(n))
			{
				same = false;
				break;
			}
		}
		
		return same;
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
		StringBuilder sbSingleCell = new StringBuilder();
		StringBuilder sbMultiCell = new StringBuilder();
		List<Symbol> lSymbols = new ArrayList<>(m_couldBeCellsForSymbol.keySet());
		Collections.sort(lSymbols);
				
		for(Symbol symbol : lSymbols)
		{
			List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
			String cellListString = cellListToString(lCells);
			
			if(lCells.size() == 1)
			{
				String markAsUnassigned = "";
				if(!m_assignedSymbols.containsKey(symbol))
				{
					markAsUnassigned = "*";
				}
				sbSingleCell.append(symbol.getGridRepresentation() + ":" + cellListString + markAsUnassigned + " ");
			}
			else
			{
				sbMultiCell.append(symbol.getGridRepresentation() + ":[" + cellListString + "] ");				
			}
		}
		
		
		return "Unresolved: " + sbMultiCell.toString().trim() + "   Resolved: " + sbSingleCell.toString().trim();
	}
	
}

//Paired symbols in a cell set which can only exist in a subset of cells. The two lists will be the same length.  
class SymbolPairRestriction {
	CellSet m_cellSet;	
	List<Symbol> m_lSymbols;
	List<Cell> m_lCells;
}
