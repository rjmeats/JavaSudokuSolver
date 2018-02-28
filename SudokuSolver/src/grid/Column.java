package grid;

public class Column extends CellSet {

	private int m_columnNumber;
	
	public Column(int columnNumber) {
		m_columnNumber = columnNumber;
	}
	
	public int getColumnNumber() {
		return m_columnNumber;
	}
	
	public String getRepresentation() {
		return "Column " + m_columnNumber; 
	}
	
}