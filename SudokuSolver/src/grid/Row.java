package grid;

public class Row extends CellSet implements Comparable<Row> {

	public Row(int rowNumber) {
		super(rowNumber);
	}
	
	public int getRowNumber() {
		return getItemNumber();
	}

	public String getRepresentation() {
		return "Row " + getRowNumber(); 
	}	

	@Override
	public int compareTo(Row row) {
		return super.compareTo(row);
	}
}

