package grid;

import java.util.TreeSet;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

import puzzle.Symbol;

public abstract class CellSet {

	private int m_itemNumber;
	Set<Cell> m_lCells;
		
	public CellSet(int itemNumber) {
		m_itemNumber = itemNumber;
		m_lCells = new TreeSet<>();
	}

	public int getItemNumber() { return m_itemNumber; }
	
	public abstract String getRepresentation();

	void addCell(Cell cell)	{
		m_lCells.add(cell);
	}

	public boolean containsCell(Cell cell) {
		return m_lCells.contains(cell);	
	}
	
	// Check that no symbol is used more than once in the cells in this cell set
	public Set<Cell> isValid() {
		Map<Symbol, Cell> symbols = new HashMap<>();
		Set<Cell> badCells = new LinkedHashSet<>();
		
		for(Cell cell : m_lCells) {
			Symbol symbol = cell.getAssignedSymbol();
			if(symbol != null) {
				if(symbols.containsKey(symbol)) {
					badCells.add(cell);
					badCells.add(symbols.get(symbol));
				}
				else {
					symbols.put(symbol, cell);
				}
			}
		}
		
		return badCells;
	}
}
