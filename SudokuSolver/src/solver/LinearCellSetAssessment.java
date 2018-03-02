package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grid.Box;
import grid.Cell;
import grid.LinearCellSet;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

abstract class LinearCellSetAssessment extends CellSetAssessment {

	LinearCellSet m_linearCellSet;
	
	LinearCellSetAssessment(LinearCellSet cellSet, SymbolsToUse symbols) {
		super(cellSet, symbols);
		m_linearCellSet = cellSet;
	}
	
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
					System.err.println("Found restricted symbol " + symbol.toString() + " in " + getRepresentation() + " and box " + lCells.get(0).getBox().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction(symbol, lCells.get(0).getBox(), m_linearCellSet);
					lRestrictions.add(restriction);
				}
			}			
		}
		
		return lRestrictions;
	}	

}
