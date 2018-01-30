import java.util.List;

public class Row extends CellSet {

	private int m_rowNumber;
	
	public Row(int rowNumber, List<CellSymbol> lSymbols) {
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
