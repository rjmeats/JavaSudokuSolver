package grid;

public class Box extends CellSet {

	private int m_boxNumber; 
	
	public Box(int boxNumber) {
		m_boxNumber = boxNumber;
	}
	
	public int getBoxNumber() {
		return m_boxNumber;
	}
	
	public String getRepresentation() {
		return "Box " + m_boxNumber; 
	}
	
}

