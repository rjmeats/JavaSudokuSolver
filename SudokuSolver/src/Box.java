import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Box extends CellSet {

	private int m_boxNumber;
	
	public Box(int boxNumber, List<Symbol> lSymbols) {
		super(lSymbols);
		m_boxNumber = boxNumber;
	}
	
	public int getBoxNumber() {
		return m_boxNumber;
	}
	
	public String getRepresentation() {
		return "Box " + m_boxNumber; 
	}
	
	public List<SymbolRestriction> findRestrictedSymbols() {
		
if(this.m_boxNumber == 8)
{
	int x = 199;
}
		List<SymbolRestriction> lRestrictions = new ArrayList<>();
		
		
		for(Symbol symbol : m_couldBeCellsForSymbol.keySet())
		{
			List<Cell> lCells = m_couldBeCellsForSymbol.get(symbol);
			if(lCells.size() == 2 || lCells.size() == 3)
			{
				HashMap<Row, Row> rowMap = new HashMap<>();
				HashMap<Column, Column> columnMap = new HashMap<>();
				for(Cell cell : lCells)
				{
					rowMap.put(cell.getRow(), cell.getRow());
					columnMap.put(cell.getColumn(), cell.getColumn());
				}
				
				if(rowMap.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_boxNumber + " and row " + lCells.get(0).getRow().getRepresentation());
					SymbolRestriction restriction = new SymbolRestriction();
					restriction.m_rowOrColumn = lCells.get(0).getRow();
					restriction.m_symbol = symbol;
					restriction.m_box = this;
					lRestrictions.add(restriction);
				}
				else if(columnMap.size() == 1)
				{
					System.err.println("Found restricted symbol " + symbol.toString() + " in box " + m_boxNumber + " and column " + lCells.get(0).getColumn().getRepresentation());
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
	Box m_box;
	CellSet m_rowOrColumn;	
}
