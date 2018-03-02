package solver;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import grid.Cell;
import grid.Column;
import grid.Box;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

class ColumnAssessment extends CellSetAssessment {

	Column m_column;
	
	public ColumnAssessment(Column column, SymbolsToUse symbols) {
		super(column, symbols);
		m_column = column;
	}

	int getSetNumber() { return m_column.getColumnNumber(); }

	public List<SymbolRestriction> findRestrictedSymbols() {
		
		List<SymbolRestriction> lRestrictions = new ArrayList<>();
		
		for(Symbol symbol : getSymbols())
		{
			List<Cell> lCells = getCouldBeCellsForSymbol(symbol);
			if(lCells.size() == 2 || lCells.size() == 3)
			{
				HashMap<Box, Box> boxMap = new HashMap<>();
				for(Cell cell : lCells)
				{
					boxMap.put(cell.getBox(), cell.getBox());
				}
				
				if(boxMap.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in column " + m_column.getColumnNumber() + " and box " + lCells.get(0).getBox().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction();
					restriction.m_box = lCells.get(0).getBox();
					restriction.m_symbol = symbol;
					restriction.m_rowOrColumn= m_column;
					lRestrictions.add(restriction);
				}
			}			
		}
		
		return lRestrictions;
	}	

}

