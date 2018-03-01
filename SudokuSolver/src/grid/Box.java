package grid;

public class Box extends CellSet {

	public Box(int boxNumber) {
		super(boxNumber);
	}
	
	public int getBoxNumber() {
		return getItemNumber();
	}
	
	public String getRepresentation() {
		return "Box " + getBoxNumber(); 
	}	
}

