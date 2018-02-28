package grid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import puzzle.Symbol;

public class Column extends CellSet {

	private int m_columnNumber;
	
	public Column(int columnNumber, List<Symbol> lSymbols) {
		super(lSymbols);
		m_columnNumber = columnNumber;
	}
	
	public int getColumnNumber() {
		return m_columnNumber;
	}
	
	public String getRepresentation() {
		return "Column " + m_columnNumber; 
	}
	
}

class ColumnAssessment extends CellSetAssessment {

	Column m_column;
	
	public ColumnAssessment(Column column, List<Symbol> lSymbols) {
		super(column, lSymbols);
		m_column = column;
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
					System.err.println("Found restricted symbol " + symbol.toString() + " in column " + m_column.getColumnNumber() + " and box " + lCells.get(0).getBox().m_box.getRepresentation());
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

