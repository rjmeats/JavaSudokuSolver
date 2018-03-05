package grid;

public class Row extends LinearCellSet implements Comparable<CellSet> {

	public Row(int rowNumber) {
		super(rowNumber);
	}
	
	public int getRowNumber() {
		return getItemNumber();
	}

	public String getRepresentation() {
		return "Row " + getRowNumber(); 
	}	

//	@Override
//	public int compareTo(CellSet row) {
//		return super.compareTo(row);
//	}
}

