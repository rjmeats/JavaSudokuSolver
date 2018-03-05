package solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import grid.Cell;
import grid.LinearCellSet;
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
					rowSet.add(cell.row());
					columnSet.add(cell.column());
				}
				
				if(rowSet.size() == 1)
				{
//					System.err.println("Found restricted symbol " + symbol.toString() + " in " + m_box.getRepresentation() + " and " + lCells.get(0).getRow().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction(symbol, m_box, lCells.get(0).row());
					lRestrictions.add(restriction);
				}
				else if(columnSet.size() == 1)
				{
//					System.err.println("Found restricted symbol " + symbol.toString() + " in " + m_box.getRepresentation() + " and " + lCells.get(0).getColumn().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction(symbol, m_box, lCells.get(0).column()); 
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
	LinearCellSet m_rowOrColumn;
	
	SymbolRestriction(Symbol symbol, Box box, LinearCellSet rowOrColumn) {
		m_symbol = symbol;
		m_box = box;
		m_rowOrColumn = rowOrColumn;
	}
	
	String getRepresentation() {
		return "Restriction on symbol " + m_symbol.getRepresentation() + " between " + m_box.getOneBasedRepresentation() + " and " + m_rowOrColumn.getOneBasedRepresentation();
	}
}
