package grid;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import puzzle.Symbol;

public abstract class CellSet implements Comparable<CellSet> {

	private int m_itemNumber;
	private String m_typeName;
	private Set<Cell> m_lCells;
		
	public CellSet(String typeName, int itemNumber) {
		m_typeName = typeName;
		m_itemNumber = itemNumber;
		m_lCells = new TreeSet<>();
	}

	private int getItemNumber() { return m_itemNumber; }
	
	public String getRepresentation() {
		return m_typeName + " " + getNumberOnlyRepresentation(); 
	}

	public String getNumberOnlyRepresentation() {
		return "" + (getItemNumber()+1);
	}
	
	void addCell(Cell cell)	{
		m_lCells.add(cell);
	}

	public boolean containsCell(Cell cell) {
		return m_lCells.contains(cell);	
	}

	public Set<Cell> getCells() {
		return new LinkedHashSet<>(m_lCells);
	}
	
	public int size() { 
		return m_lCells.size();		
	}
	
	// Check that no symbol is used more than once in the cells in this cell set - return the cells where there is duplication
	public List<Cell> getListOfIncompatibleCells() {
		Map<Symbol, Cell> symbols = new HashMap<>();
		Set<Cell> sameSymbolCells = new LinkedHashSet<>();
		
		for(Cell cell : m_lCells) {
			Symbol symbol = cell.getAssignedSymbol();
			if(symbol != null) {
				if(symbols.containsKey(symbol)) {
					Cell sameSymbolCell = symbols.get(symbol);
					sameSymbolCells.add(sameSymbolCell);
					sameSymbolCells.add(cell);
				}
				else {
					symbols.put(symbol, cell);
				}
			}
		}
		
		return new ArrayList<>(sameSymbolCells);
	}
	
	public Set<Cell> getCellsNotIn(CellSet cs) {
		return m_lCells.stream().filter(cell -> (!cs.containsCell(cell))).collect(Collectors.toSet());
	}

	public int compareTo(CellSet cellset) {
		int diff = m_typeName.compareTo(cellset.m_typeName);
		if(diff == 0) {
			diff = getItemNumber() - cellset.getItemNumber();
		}
		
		return diff;
	}
}
