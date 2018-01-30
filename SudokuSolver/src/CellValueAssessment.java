import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

public class CellValueAssessment {

	Cell m_cell;	
	CellSymbol m_assignment;
	AssignmentMethod m_method;
	int m_assignedAtStepNumber;
	
	private HashMap<CellSymbol, CellSymbol> m_mapCouldBe;
	private HashMap<CellSymbol, CellSymbol> m_mapRuledOut;
		
	CellValueAssessment(Cell cell, List<CellSymbol> lSymbols)
	{
		m_cell = cell;
		m_assignment = null;
		m_method = AssignmentMethod.NotAssigned;
		
		m_mapCouldBe = new HashMap<>();
		m_mapRuledOut = new HashMap<>();
		
		for(CellSymbol symbol : lSymbols)
		{
			m_mapCouldBe.put(symbol, symbol);
		}
	}
	
	boolean isAssigned()
	{
		return m_assignment != null;
	}
	
	void setAsAssigned(AssignmentMethod method, CellSymbol symbol)
	{
		m_method = method;
		m_assignment = symbol;
		m_assignedAtStepNumber = 0;

		for(CellSymbol otherSymbol : m_mapCouldBe.keySet())
		{
			if(symbol != otherSymbol)
			{
				m_mapRuledOut.put(otherSymbol, otherSymbol);
			}
		}
		m_mapCouldBe.clear();
		m_mapCouldBe.put(symbol, symbol);
	}
	
	boolean couldBe(CellSymbol symbol)
	{
		return m_mapCouldBe.containsKey(symbol);
	}

	boolean isRuledOut(CellSymbol symbol)
	{
		return m_mapRuledOut.containsKey(symbol);
	}
	
	boolean ruleOut(CellSymbol symbol)
	{
		boolean changed = false;
		if(!isRuledOut(symbol))
		{
			if(couldBe(symbol))
			{
				m_mapRuledOut.put(symbol,symbol);
				m_mapCouldBe.remove(symbol);
				changed = true;
				Puzzle.L.info(".. ruled out cell " + m_cell.m_cellNumber + " : " + symbol.getRepresentation());				
			}
			else
			{
				// ???? Don't expect to hit this
			}
		}
		
		Puzzle.L.info(".. for cell " + m_cell.m_cellNumber + " symbol-could-be list = " + symbolMapToString(m_mapCouldBe) + ":  ruled-out list = " + symbolMapToString(m_mapRuledOut));
		
		return changed;
	}
	
	int couldBeCount()
	{
		return m_mapCouldBe.size();
	}
	
	String couldBeValuesString()
	{
		return symbolMapToString(m_mapCouldBe);
	}
	
	private static String symbolMapToString(HashMap<CellSymbol, CellSymbol> map)
	{
		List<CellSymbol> l = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for(CellSymbol symbol: map.keySet())
		{
			l.add(symbol);
		}

		Collections.sort(l);
		for(CellSymbol symbol: l)
		{
			sb.append(symbol.getRepresentation()).append(" ");
		}
		return sb.toString().trim();
	}
}
