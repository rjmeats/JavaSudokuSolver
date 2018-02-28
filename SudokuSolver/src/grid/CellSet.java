package grid;

import java.util.TreeSet;
import java.util.Set;

public abstract class CellSet {

	Set<Cell> m_lCells;
		
	public CellSet() {
		m_lCells = new TreeSet<>();
	}

	public abstract String getRepresentation();

	void addCell(Cell cell)	{
		m_lCells.add(cell);
	}

	public boolean containsCell(Cell cell)
	{
		return m_lCells.contains(cell);	
	}
		
}
