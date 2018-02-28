package solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import grid.Box;
import puzzle.Symbol;

public class BoxAssessment extends CellSetAssessment {

	Box m_box;
	
	public BoxAssessment(Box box, List<Symbol> lSymbols) {
		super(box, lSymbols);
		m_box = box;
	}

	int getSetNumber() { return m_box.getBoxNumber(); }
	
	public List<SymbolRestriction> findRestrictedSymbols() {
		
		List<SymbolRestriction> lRestrictions = new ArrayList<>();		
		
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			List<CellAssessment> lCells = m_couldBeCellsForSymbol.get(symbol);
			if(lCells.size() == 2 || lCells.size() == 3)
			{
				Set<RowAssessment> rowSet = new HashSet<>();
				Set<ColumnAssessment> columnSet = new HashSet<>();
				for(CellAssessment cell : lCells)
				{
					rowSet.add(cell.getRow());
					columnSet.add(cell.getColumn());
				}
				
				if(rowSet.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_box.getBoxNumber() + " and row " + lCells.get(0).getRow().m_row.getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction();
					restriction.m_rowOrColumn = lCells.get(0).getRow();
					restriction.m_symbol = symbol;
					restriction.m_box = this;
					lRestrictions.add(restriction);
				}
				else if(columnSet.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_box.getBoxNumber() + " and column " + lCells.get(0).getColumn().m_column.getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction(); 
					restriction.m_rowOrColumn = lCells.get(0).getColumn();
					restriction.m_symbol = symbol;
					restriction.m_box = this;
					lRestrictions.add(restriction);
				}
			}			
		}
		
		return lRestrictions;
	}	
}

class SymbolRestriction {
	Symbol m_symbol;
	BoxAssessment m_box;
	CellSetAssessment m_rowOrColumn;	
}
