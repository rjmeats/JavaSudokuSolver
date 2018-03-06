package grid;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import puzzle.Symbol;

public abstract class CellSet {

	private int m_itemNumber;
	private Set<Cell> m_lCells;
		
	public CellSet(int itemNumber) {
		m_itemNumber = itemNumber;
		m_lCells = new TreeSet<>();
	}

	public int getItemNumber() { return m_itemNumber; }
	
	public abstract String getRepresentation();
	public abstract String getOneBasedRepresentation();

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
	public Set<Cell> getIncompatibleCells() {
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
		
		return sameSymbolCells;
	}
	
	public Set<Cell> getCellsNotIn(CellSet cs) {
		return m_lCells.stream().filter(cell -> (!cs.containsCell(cell))).collect(Collectors.toSet());
	}

	public int compareTo(CellSet cellset) {
		return getItemNumber() - cellset.getItemNumber();
	}
}
