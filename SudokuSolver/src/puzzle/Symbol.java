package puzzle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public enum Symbol {

	One("1"), Two("2"), Three("3"), Four("4"), Five("5"), Six("6"), Seven("7"), Eight("8"), Nine("9");
	
	private String m_representation;
	
	Symbol(String representation) {
		m_representation = representation;
	}

	public String toString() {
		return m_representation;
	}
	
	String getGridRepresentation() {
		return m_representation;
	}
	
	static Symbol toSymbol(String representation)
	{
		Symbol symbol = null;
		
		for(Symbol cs : Symbol.values())
		{
			if(cs.m_representation.equals(representation))
			{
				symbol = cs;
			}
		}
		
		return symbol;
	}

	static Symbol toSymbol(char charRepresentation)
	{
		return toSymbol(charRepresentation + "");
	}
	
	public static String symbolMapToString(HashMap<Symbol, ?> map)		// Ignores values though, so not a great name. ????
	{
		List<Symbol> l = new ArrayList<>();
		for(Symbol symbol: map.keySet())
		{
			l.add(symbol);
		}

		return(symbolListToString(l));
	}

	public static String symbolListToString(List<Symbol> l)
	{
		StringBuilder sb = new StringBuilder();
		List<Symbol> lSorted = new ArrayList<>(l);
		Collections.sort(lSorted);
		for(Symbol symbol: lSorted)
		{
			sb.append(symbol.getGridRepresentation()).append(" ");
		}
		return sb.toString().trim();
	}
	
	static class SortBySymbol implements Comparator<Symbol>
	{
	    // Used for sorting in ascending order of
	    // roll number
	    public int compare(Symbol s1, Symbol s2)
	    {
	        return s1.ordinal() - s2.ordinal();
	    }
	}

}
