package grid;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 *	Abstract class representing a related set of cells in a grid forming a column, row or box. 
 *
 *	Each cell in the CellSet is assigned to a different symbol in a valid grid.
 */

public abstract class CellSet implements Comparable<CellSet> {

	// Each cellset has a type identifier (row/column/box) and an item number within the set.
	// NB Internally (when used for grid calculations), cellset numbers start at 0. 
	// But for ease of human comprehension, show them as starting from 1 in grid-based output.  
	private String m_typeName;
	private int m_itemNumber;
	
	// The cells forming the cellset.
	private Set<Cell> m_lCells;
		
	CellSet(String typeName, int itemNumber) {
		m_typeName = typeName;
		m_itemNumber = itemNumber;
		m_lCells = new TreeSet<>();		// TreeSet keeps the set ordered, based on comparedTo method
	}

	void addCell(Cell cell)	{
		m_lCells.add(cell);
	}

	private int itemNumber() 			{ return m_itemNumber; }
	private int itemNumberForDisplay() 	{ return m_itemNumber+1; }

	public boolean containsCell(Cell cell) {
		return m_lCells.contains(cell);	
	}

	public Set<Cell> cells() {
		return new TreeSet<>(m_lCells);
	}
	
	public int size() { 
		return m_lCells.size();		
	}
	
	// Use 1-based numbering for showing a cell number in external displays of grid contents.
	public String numberOnlyRepresentation() {
		return "" + itemNumberForDisplay();
	}

	public String getRepresentation() {
		return m_typeName.toLowerCase() + " " + numberOnlyRepresentation(); 
	}

	// For debuggers only
	public String toString() {
		return "Cellset: " + m_typeName + " " + itemNumber();
	}

	public int compareTo(CellSet cellset) {
		int diff = m_typeName.compareTo(cellset.m_typeName);
		if(diff == 0) {
			diff = itemNumber() - cellset.itemNumber();
		}
		return diff;
	}

	/**
	 * Check whether the assignments to this cellset are compatible with each other.
	 *  
	 * @return A list of the cells in the cellset which are not compatible, because a symbol appears more than once in the cellset. 
	 * If the cellset is valid, an empty list will be returned.
	 */
	public Set<Cell> listOfIncompatibleCells() {
		// Keep track of the symbols which are spoken for, and which cells clash with other cells in the cellset.
		Map<Symbol, Cell> symbolsUsed = new HashMap<>();
		Set<Cell> incompatibleCells = new LinkedHashSet<>();		// Use a set to prevent duplicates
		
		for(Cell cell : m_lCells) {
			Symbol symbol = cell.assignedSymbol();
			if(symbol != null) {
				if(symbolsUsed.containsKey(symbol)) {
					// We already found a cell in this cellset assigned to this symbol. Not allowed - add both
					// cells to the list of clashing cells.
					Cell clashingCell = symbolsUsed.get(symbol);
					incompatibleCells.add(clashingCell);
					incompatibleCells.add(cell);
				}
				else {
					// This is the first cell we've found in the cellset using this symbol.
					symbolsUsed.put(symbol, cell);
				}
			}
			else {
				// No assignment has been made to this cell yet.
			}
		}
		
		return incompatibleCells;
	}
	
	/**
	 * Identify the cells which are in this cellset but not in a second cellset.
	 *  
	 * @param cs2 The second cellset
	 * @return A set containing the cells which are in this cellset but not the second one.
	 */
	public Set<Cell> cellsNotIn(CellSet cs2) {
		// Functional approach looking through each cell in the cellset
		return cells().stream()
				.filter(cell -> (!cs2.containsCell(cell)))		// Only keep cells which are not in the second cellset	
				.collect(Collectors.toSet());
	}
}
