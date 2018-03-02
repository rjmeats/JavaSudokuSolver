package grid;

public class Box extends CellSet implements Comparable<Box> {

	public Box(int boxNumber) {
		super(boxNumber);
	}
	
	public int getBoxNumber() {
		return getItemNumber();
	}
	
	public String getRepresentation() {
		return "Box " + getBoxNumber(); 
	}	
	
	@Override
	public int compareTo(Box box) {
		return super.compareTo(box);
	}
}

