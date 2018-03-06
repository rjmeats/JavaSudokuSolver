package puzzle;
import grid.Cell;

public class Assignment {

	private Cell m_cell;
	private Symbol m_symbol;
	private AssignmentMethod m_method;
	private String m_assignmentDetail;
	private int m_stepNumber;
	
	public Assignment(Cell cell, Symbol symbol, AssignmentMethod method, String assignmentDetail, int stepNumber) {
		m_cell = cell;
		m_symbol = symbol;
		m_method = method;
		m_assignmentDetail = assignmentDetail;
		m_stepNumber = stepNumber;
	}
		
	public Cell cell() 					{ return m_cell; }
	public Symbol symbol() 				{ return m_symbol; }
	public AssignmentMethod method() 	{ return m_method; }
	public String detail() 				{ return m_assignmentDetail; }
	public int stepNumber() 			{ return m_stepNumber; }
	
	public String toString() {
		return "Assignment: Cell=" + m_cell.getOneBasedGridLocationString() + ", Symbol=" + m_symbol.toString() + ", method=" + m_method.toString() + ", detail=" + m_assignmentDetail + ", step=" + m_stepNumber; 
	}
}
