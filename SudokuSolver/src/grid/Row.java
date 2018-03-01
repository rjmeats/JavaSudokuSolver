package grid;

public class Row extends CellSet {

	public Row(int rowNumber) {
		super(rowNumber);
	}
	
	public int getRowNumber() {
		return getItemNumber();
	}

	public String getRepresentation() {
		return "Row " + getRowNumber(); 
	}	
}

