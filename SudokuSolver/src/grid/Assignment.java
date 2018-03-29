package grid;

/**
 * Records details of the assignment of a specific symbol to a specific cell.
 */

public class Assignment {

	private Cell m_cell;
	private Symbol m_symbol;
	
	// Was the assignment part of the initial puzzle ('Given') or deduced/guessed by later processing ? 
	private AssignmentMethod m_method; 
	
	// Items providing diagnostics and explanations of how/when the assignment occurred.
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
	
	public String description() {
		return "Assignment: cell=" + m_cell.getGridLocationString() + ", symbol=" + m_symbol.toString() + ", method=" + m_method.toString() + ", detail=" + m_assignmentDetail + ", step=" + m_stepNumber; 
	}
}
