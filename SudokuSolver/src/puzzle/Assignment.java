package puzzle;
import grid.Cell;

public class Assignment {

	private Cell m_cell;
	private Symbol m_symbol;
	private AssignmentMethod m_method;	
	private int m_stepNumber;
	
	public Assignment(Cell cell, Symbol symbol, AssignmentMethod method, int stepNumber)
	{
		m_cell = cell;
		m_symbol = symbol;
		m_method = method;
		m_stepNumber = stepNumber;
	}
	
	
	public Cell getCell() { return m_cell; }
	public Symbol getSymbol() { return m_symbol; }
	public AssignmentMethod getMethod() { return m_method; }
	public int getStepNumber() { return m_stepNumber; }
	
	public String toString()
	{
		return "Assignment: Cell=" + m_cell.getColumnAndRowLocationString() + ", Symbol=" + m_symbol.toString() + ", method=" + m_method.toString() + ", step=" + m_stepNumber; 
	}
}
