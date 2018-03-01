package grid;

public class Column extends CellSet {

	public Column(int columnNumber) {
		super(columnNumber);
	}
	
	public int getColumnNumber() {
		return getItemNumber();
	}
	
	public String getRepresentation() {
		return "Column " + getColumnNumber(); 
	}	
}