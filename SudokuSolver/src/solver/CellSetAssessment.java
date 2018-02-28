package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import grid.Cell;
import grid.CellSet;
import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Puzzle;
import puzzle.Symbol;

abstract class CellSetAssessment {
	CellSet m_cellSet;
	Set<CellAssessment> m_lCellAssessments;
	
	HashMap<Symbol, Assignment> m_assignedSymbols;
	HashMap<Symbol, List<CellAssessment>> m_couldBeCellsForSymbol;

	public CellSetAssessment(CellSet cellSet, List<Symbol> lSymbols) {
		m_cellSet = cellSet;
		m_lCellAssessments = new TreeSet<>();
		m_assignedSymbols = new HashMap<>();
		
		m_couldBeCellsForSymbol = new HashMap<>();		
		for(Symbol symbol : lSymbols)
		{
			m_couldBeCellsForSymbol.put(symbol, new ArrayList<CellAssessment>());
		}
	}

	void addCell(CellAssessment ca)	{
		m_lCellAssessments.add(ca);
		for(List<CellAssessment> lCells : m_couldBeCellsForSymbol.values())
		{
			lCells.add(ca);
		}
	}

	boolean symbolAlreadyAssigned(Symbol symbol)
	{
		return m_assignedSymbols.containsKey(symbol);
	}
	
	// A particular symbol has been assigned to a cell, so mark it as ruled-out for other cells in this set.
	void markAsAssigned(Assignment assignment, CellAssessment ca)
	{
		Symbol symbol = assignment.getSymbol();
//		Cell assignmentCell = assignment.getCell();
		
		Puzzle.L.info(".. marking symbol " + symbol.toString() + " as assigned to cell " + ca.m_cell.getCellNumber() + " in cellset " + m_cellSet.getRepresentation());
		
		// Add to the list of symbols in this set which are now assigned.
		m_assignedSymbols.put(symbol, assignment);

		List<CellAssessment> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.clear();
//		lCellsForThisSymbol.add(assignmentCell);
		lCellsForThisSymbol.add(ca);

		// And go through all the other cells sharing a cell set with this set, ruling out this symbol for their use.
		ca.getRow().ruleOutSymbolFromOtherCells(symbol, ca);
		ca.getColumn().ruleOutSymbolFromOtherCells(symbol, ca);
		ca.getBox().ruleOutSymbolFromOtherCells(symbol, ca);

		// And go through all the symbols for other cell-sets sharing a cell set with this set, ruling out this symbol for use in their other sets.
		ca.getRow().ruleOutCouldBeCellsForSymbol(symbol, ca);
		ca.getColumn().ruleOutCouldBeCellsForSymbol(symbol, ca);
		ca.getBox().ruleOutCouldBeCellsForSymbol(symbol, ca);
	}

	void ruleOutSymbolFromOtherCells(Symbol symbol, CellAssessment assignmentCell)
	{
		for(CellAssessment otherCell : m_lCellAssessments)
		{
			if(otherCell != assignmentCell)
			{
				Puzzle.L.info(".. ruling out " + symbol.toString() + " for cell " + otherCell.m_cell.getCellNumber() + " in cell-set " + m_cellSet.getRepresentation());				
				otherCell.ruleOut(symbol);
				if(this != otherCell.getRow()) otherCell.getRow().ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.getColumn()) otherCell.getColumn().ruleOutSymbolForCell(symbol, otherCell);
				if(this != otherCell.getBox()) otherCell.getBox().ruleOutSymbolForCell(symbol, otherCell);
			}
		}
		
	}

	void ruleOutSymbolForCell(Symbol symbol, CellAssessment cell)
	{
		List<CellAssessment> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(symbol);
		lCellsForThisSymbol.remove(cell);
		Puzzle.L.info(".. ruling out " + symbol.toString() + " for cell " + cell.m_cell.getCellNumber() + " in cell-set " + m_cellSet.getRepresentation());
  		Puzzle.L.info(".. cell-could-be list for symbol = " + cellListToString(lCellsForThisSymbol));
	}

	void ruleOutCouldBeCellsForSymbol(Symbol symbol, CellAssessment assignmentCell)
	{
		for(Symbol otherSymbol : m_couldBeCellsForSymbol.keySet())
		{
			if(otherSymbol != symbol)
			{				
				List<CellAssessment> lCellsForThisSymbol = m_couldBeCellsForSymbol.get(otherSymbol);
				Puzzle.L.info(".. ruling out cell " + assignmentCell.m_cell.getCellNumber() + " for symbol " + otherSymbol.toString() + " in cell-set " + m_cellSet.getRepresentation());				
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
				List<CellAssessment> lCells = m_couldBeCellsForSymbol.get(symbol);
				if(lCells.size() == 1)
				{
					assignableCell = new Assignment(lCells.get(0).m_cell, symbol, AssignmentMethod.AssignedSymbolToCellSet, stepNumber);
					break;
				}
			}
		}
		
		return assignableCell;
	}

	boolean isComplete()
	{
		return m_assignedSymbols.size() == m_lCellAssessments.size();
	}
	
	boolean ruleOutSymbolOutsideBox(SymbolRestriction restriction)
	{
		boolean changedState = false;
		// For cells not in the restriction box, rule out the symbol.
		for(CellAssessment cell : m_lCellAssessments)
		{
			if(!restriction.m_box.m_cellSet.containsCell(cell.m_cell))
			{
				if(!cell.isRuledOut(restriction.m_symbol))
				{
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.m_cell.getColumnAndRowLocationString());				
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
		for(CellAssessment cell : m_lCellAssessments)
		{
			if(!restriction.m_rowOrColumn.m_cellSet.containsCell(cell.m_cell))
			{
				if(!cell.isRuledOut(restriction.m_symbol))
				{
System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.m_cell.getColumnAndRowLocationString());				
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
			List<CellAssessment> lCells1 = m_couldBeCellsForSymbol.get(symbol1);
			if(lCells1.size() > 1)
			{
				for(Symbol symbol2 : m_couldBeCellsForSymbol.keySet())
				{
					if(symbol2.ordinal() > symbol1.ordinal())
					{
						List<CellAssessment> lCells2 = m_couldBeCellsForSymbol.get(symbol2);
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
									List<CellAssessment> lCells3 = m_couldBeCellsForSymbol.get(symbol3);
									if(lCells3.size() > 1)
									{
										// We have a combination of three symbols to investigate ...
										List<Symbol> l3 = new ArrayList<>(l2); l3.add(symbol3); 
										lCombinations.add(l3);
										for(Symbol symbol4 : m_couldBeCellsForSymbol.keySet())
										{
											if(symbol4.ordinal() > symbol3.ordinal())
											{
												List<CellAssessment> lCells4 = m_couldBeCellsForSymbol.get(symbol4);
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

		System.err.println("Found " + lCombinations.size() + " symbol combinations for set " + m_cellSet.getRepresentation());
		for(List<Symbol> lCombination : lCombinations)
		{
			List<CellAssessment> lCellsForCombination = getSymbolCombinationCells(lCombination);
			boolean foundSet = (lCombination.size() == lCellsForCombination.size());
			if(foundSet)
			{
				System.err.println((foundSet ? "** " : "   ") + "Symbol combination: " + Symbol.symbolListToString(lCombination) + " covers cells " +  CellSetAssessment.cellListToString(lCellsForCombination));
				
				SymbolSetRestriction restriction = new SymbolSetRestriction();
				restriction.m_cellSet = this;
				restriction.m_lCells = lCellsForCombination;
				restriction.m_lSymbols = lCombination;
				l.add(restriction);
			}
		}		
		
		return l;
	}

	List<CellAssessment> getSymbolCombinationCells(List<Symbol> lCombination)
	{
		Set<CellAssessment> cells = new TreeSet<>();
		for(Symbol symbol : lCombination)
		{
			List<CellAssessment> l = m_couldBeCellsForSymbol.get(symbol);
			for(CellAssessment cell : l)
			{
				cells.add(cell);
			}
		}
				
		return new ArrayList<CellAssessment>(cells);
	}

	boolean ruleOutAllCellsBut(Symbol symbol, List<CellAssessment> lCells)
	{		
		boolean changed = false;
		
		List<CellAssessment> lUnwantedCells = new ArrayList<>();
		List<CellAssessment> lCouldBeCellsForSymbol = m_couldBeCellsForSymbol.get(symbol);
		for(CellAssessment couldBeCell : lCouldBeCellsForSymbol)
		{
			if(!lCells.contains(couldBeCell))
			{
				lUnwantedCells.add(couldBeCell);
			}
		}
		
		for(CellAssessment unwantedCell : lUnwantedCells)
		{
			if(lCouldBeCellsForSymbol.contains(unwantedCell))
			{
				lCouldBeCellsForSymbol.remove(unwantedCell);
				changed = true;
			}
		}
		
		return changed;
	}

	boolean ruleOutCellFromOtherSymbols(CellAssessment cell, List<Symbol> lSymbols)
	{
		boolean changed = false;
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			if(!lSymbols.contains(symbol))
			{
				// Can't be assigned to this cell
				List<CellAssessment> lCells = m_couldBeCellsForSymbol.get(symbol);
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

	String getSymbolAssignmentSummary()
	{
		StringBuilder sbSingleCell = new StringBuilder();
		StringBuilder sbMultiCell = new StringBuilder();
		List<Symbol> lSymbols = new ArrayList<>(m_couldBeCellsForSymbol.keySet());
		Collections.sort(lSymbols);
				
		for(Symbol symbol : lSymbols)
		{
			List<CellAssessment> lCellAssessments = m_couldBeCellsForSymbol.get(symbol);
			String cellListString = cellListToString(lCellAssessments);
			
			if(lCellAssessments.size() == 1)
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
	
	public static String cellListToString(List<CellAssessment> l)
	{
		StringBuilder sb = new StringBuilder();
		Collections.sort(l, new CellAssessment.SortByCellNumber());
		for(CellAssessment cell: l)
		{
			sb.append(cell.m_cell.getCellNumber()).append(" ");
		}
		return sb.toString().trim();
	}

	
}
		
//Paired symbols in a cell set which can only exist in a subset of cells. The two lists will be the same length.  
class SymbolSetRestriction {
	CellSetAssessment m_cellSet;	
	List<Symbol> m_lSymbols;
	List<CellAssessment> m_lCells;
	
	List<CellSetAssessment> getAffectedCellSets()
	{
		Set<CellSetAssessment> set = new TreeSet<>();
		
		for(CellAssessment cell : m_lCells)
		{
			set.add(cell.getBox());
			set.add(cell.getRow());
			set.add(cell.getColumn());
		}
		return new ArrayList<CellSetAssessment>(set);		
	}
}

