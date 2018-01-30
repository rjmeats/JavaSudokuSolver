import java.util.List;

public class Column extends CellSet {

	private int m_columnNumber;
	
	public Column(int columnNumber, List<CellSymbol> lSymbols) {
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
