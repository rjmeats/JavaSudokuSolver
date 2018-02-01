import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public enum CellSymbol {

	One("1"), Two("2"), Three("3"), Four("4"), Five("5"), Six("6"), Seven("7"), Eight("8"), Nine("9");
	
	private String m_representation;
	
	CellSymbol(String representation) {
		m_representation = representation;
	}
	
	String getRepresentation() {
		return m_representation;
	}
	
	static CellSymbol toCellSymbol(String representation)
	{
		CellSymbol symbol = null;
		
		for(CellSymbol cs : CellSymbol.values())
		{
			if(cs.m_representation.equals(representation))
			{
				symbol = cs;
			}
		}
		
		return symbol;
	}

	static CellSymbol toCellSymbol(char c)
	{
		return toCellSymbol(c + "");
	}
	
	public static String symbolMapToString(HashMap<CellSymbol, CellSymbol> map)
	{
		List<CellSymbol> l = new ArrayList<>();
		for(CellSymbol symbol: map.keySet())
		{
			l.add(symbol);
		}

		return(symbolListToString(l));
	}

	public static String symbolListToString(List<CellSymbol> l)
	{
		StringBuilder sb = new StringBuilder();
		List<CellSymbol> lSorted = new ArrayList<>(l);
		Collections.sort(lSorted);
		for(CellSymbol symbol: lSorted)
		{
			sb.append(symbol.getRepresentation()).append(" ");
		}
		return sb.toString().trim();
	}
}
