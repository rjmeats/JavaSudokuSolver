import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;

public abstract class CellSet {

	Set<Cell> m_lCells;
	HashMap<Symbol, Assignment> m_assignedSymbols;
	HashMap<Symbol, List<Cell>> m_couldBeCellsForSymbol;
		
	public CellSet(List<Symbol> lSymbols) {
		m_lCells = new TreeSet<>();
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

	// Goes up to combinations of 4 - how to generalise to n ?
	List<SymbolSetRestriction> findRestrictedSymbolSets()
	{
		List<SymbolSetRestriction> l = new ArrayList<>();
		// Generate combinations of 2, 3 and 4 unassigned symbols. If the combination has n symbols and between them these can only
		// be placed in n cells, then we have a restricted symbol set.
		
		List<List<Symbol>> lCombinations = new ArrayList<>();
		
		for(Symbol symbol1 : m_couldBeCellsForSymbol.keySet())
		{
			List<Cell> lCells1 = m_couldBeCellsForSymbol.get(symbol1);
			if(lCells1.size() > 1)
			{
				for(Symbol symbol2 : m_couldBeCellsForSymbol.keySet())
				{
					if(symbol2.ordinal() > symbol1.ordinal())
					{
						List<Cell> lCells2 = m_couldBeCellsForSymbol.get(symbol2);
						if(lCells2.size() > 1)
						{
							// We have a combination of two symbols to investigate ...
							List<Symbol> l2 = new ArrayList<>();
							l2.add(symbol1); l2.add(symbol2);
							lCombinations.add(l2);
							
							for(Symbol symbol3 : m_couldBeCellsForSymbol.keySet())
							{
								if(symbol3.ordinal() > symbol2.ordinal())
								{
									List<Cell> lCells3 = m_couldBeCellsForSymbol.get(symbol3);
									if(lCells3.size() > 1)
									{
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);
										for(Symbol symbol4 : m_couldBeCellsForSymbol.keySet())
										{
											if(symbol4.ordinal() > symbol3.ordinal())
											{
												List<Cell> lCells4 = m_couldBeCellsForSymbol.get(symbol4);
												if(lCells4.size() > 1)
												{
													// We have a combination of four symbols to investigate ...
													List<Symbol> l4 = new ArrayList<>(l3); l4.add(symbol4); 
													lCombinations.add(l4);													
												}
											}
										}
										
									}
								}
							}
						}
					}
				}
				
			}			
		}

		System.err.println("Found " + lCombinations.size() + " symbol combinations for set " + getRepresentation());
		for(List<Symbol> lCombination : lCombinations)
		{
			List<Cell> lCellsForCombination = getSymbolCombinationCells(lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet)
			{
				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolListToString(lCombination) + " covers cells " +  CellSet.cellListToString(lCellsForCombination));
				
				SymbolSetRestriction restriction = new SymbolSetRestriction();
				restriction.m_cellSet = this;
				restriction.m_lCells = lCellsForCombination;
				restriction.m_lSymbols = lCombination;
				l.add(restriction);
			}
		}		
		
		return l;
	}

	List<Cell> getSymbolCombinationCells(List<Symbol> lCombination)
	{
		Set<Cell> cells = new TreeSet<>();
		for(Symbol symbol : lCombination)
		{
			List<Cell> l = this.m_couldBeCellsForSymbol.get(symbol);
			for(Cell cell : l)
			{
				cells.add(cell);
			}
		}
				
		return new ArrayList<Cell>(cells);
	}

	boolean ruleOutAllCellsBut(Symbol symbol, List<Cell> lCells)
	{		
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

	public static String cellListToString(List<Cell> l)
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
class SymbolSetRestriction {
	CellSet m_cellSet;	
	List<Symbol> m_lSymbols;
	List<Cell> m_lCells;
	
	List<CellSet> getAffectedCellSets()
	{
		Set<CellSet> set = new TreeSet<>();
		
		for(Cell cell : m_lCells)
		{
			set.add(cell.getBox());
			set.add(cell.getRow());
			set.add(cell.getColumn());
		}
		return new ArrayList<CellSet>(set);		
	}
}
