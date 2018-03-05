package grid;

public class Column extends LinearCellSet implements Comparable<CellSet> {

	public Column(int columnNumber) {
		super(columnNumber);
	}
	
	public int getColumnNumber() {
		return getItemNumber();
	}
	
	public String getRepresentation() {
		return "Column " + getColumnNumber(); 
	}	
	
//	@Override
//	public int compareTo(Column column) {
//		return super.compareTo(column);
//	}
}