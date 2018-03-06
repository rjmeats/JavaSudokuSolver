package grid;

public class Row extends CellSet implements Comparable<CellSet> {

	public Row(int rowNumber) {
		super(rowNumber);
	}
	
	public int getRowNumber() {
		return getItemNumber();
	}

	public String getRepresentation() {
		return "Row " + getRowNumber(); 
	}	

	public String getOneBasedRepresentation() {
		return "Row " + (getRowNumber()+1); 
	}	

//	@Override
//	public int compareTo(CellSet row) {
//		return super.compareTo(row);
//	}
}

