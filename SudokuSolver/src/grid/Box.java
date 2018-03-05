package grid;

public class Box extends CellSet implements Comparable<CellSet> {

	public Box(int boxNumber) {
		super(boxNumber);
	}
	
	public int getBoxNumber() {
		return getItemNumber();
	}
	
	public String getRepresentation() {
		return "Box " + getBoxNumber(); 
	}	
	
	public String getOneBasedRepresentation() {
		return "Box " + (getBoxNumber()+1); 
	}	
	
//	@Override
//	public int compareTo(Box box) {
//		return super.compareTo(box);
//	}
}

