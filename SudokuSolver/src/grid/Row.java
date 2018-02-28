package grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import puzzle.Symbol;

public class Row extends CellSet {

	private int m_rowNumber;
	
	public Row(int rowNumber, List<Symbol> lSymbols) {
		super(lSymbols);
		m_rowNumber = rowNumber;
	}
	
	public int getRowNumber() {
		return m_rowNumber;
	}

	public String getRepresentation() {
		return "Row " + m_rowNumber; 
	}
	
}

class RowAssessment extends CellSetAssessment {

	Row m_row;
	
	public RowAssessment(Row row, List<Symbol> lSymbols) {
		super(row, lSymbols);
		m_row = row;
	}

	public List<SymbolRestriction> findRestrictedSymbols() {
		
		List<SymbolRestriction> lRestrictions = new ArrayList<>();
		
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			List<CellAssessment> lCells = m_couldBeCellsForSymbol.get(symbol);
			if(lCells.size() == 2 || lCells.size() == 3)
			{
				HashMap<BoxAssessment, BoxAssessment> boxMap = new HashMap<>();
				for(CellAssessment cell : lCells)
				{
					boxMap.put(cell.getBox(), cell.getBox());
				}
				
				if(boxMap.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in row " + m_row.getRowNumber() + " and box " + lCells.get(0).getBox().m_box.getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction();
					restriction.m_box = lCells.get(0).getBox();
					restriction.m_symbol = symbol;
					restriction.m_rowOrColumn= this;
					lRestrictions.add(restriction);
				}
			}			
		}
		
		return lRestrictions;
	}	
}

