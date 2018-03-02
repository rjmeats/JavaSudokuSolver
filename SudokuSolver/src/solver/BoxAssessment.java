package solver;

import java.util.ArrayList;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import grid.Cell;
import grid.CellSet;
import grid.Box;
import grid.Row;
import grid.Column;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

public class BoxAssessment extends CellSetAssessment {

	Box m_box;
	
	public BoxAssessment(Box box, SymbolsToUse symbols) {
		super(box, symbols);
		m_box = box;
	}

	int getSetNumber() { return m_box.getBoxNumber(); }
	
	public List<SymbolRestriction> findRestrictedSymbols() {
		
		List<SymbolRestriction> lRestrictions = new ArrayList<>();		
		
		for(Symbol symbol : getSymbols())
		{
			List<Cell> lCells = getCouldBeCellsForSymbol(symbol);
			if(lCells.size() == 2 || lCells.size() == 3)
			{
				Set<Row> rowSet = new HashSet<>();
				Set<Column> columnSet = new HashSet<>();
				for(Cell cell : lCells)
				{
					rowSet.add(cell.getRow());
					columnSet.add(cell.getColumn());
				}
				
				if(rowSet.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_box.getBoxNumber() + " and row " + lCells.get(0).getRow().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction();
					restriction.m_rowOrColumn = lCells.get(0).getRow();
					restriction.m_symbol = symbol;
					restriction.m_box = m_box;
					lRestrictions.add(restriction);
				}
				else if(columnSet.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_box.getBoxNumber() + " and column " + lCells.get(0).getColumn().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction(); 
					restriction.m_rowOrColumn = lCells.get(0).getColumn();
					restriction.m_symbol = symbol;
					restriction.m_box = m_box;
					lRestrictions.add(restriction);
				}
			}			
		}
		
		return lRestrictions;
	}	
}

class SymbolRestriction {
	Symbol m_symbol;
	Box m_box;
	CellSet m_rowOrColumn;	
}
