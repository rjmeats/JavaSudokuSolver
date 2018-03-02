package puzzle;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Set;
import java.util.List;

public class Symbol implements Comparable<Symbol> {

	private String m_representation;
	private int m_ordinal;
	
	Symbol(String representation, int ordinal) {
		m_representation = representation;
		m_ordinal = ordinal;
	}

	public String getRepresentation() {
		return m_representation;
	}

	public int ordinal() {
		return m_ordinal;
	}
	
	public String toString() {
		return m_representation;
	}
	
	public static String symbolSetToString(Set<Symbol> set) {
		return(symbolListToString(new ArrayList<Symbol>(set)));
	}

	public static String symbolListToString(List<Symbol> l)
	{
		List<Symbol> lSorted = new ArrayList<>(l);
		Collections.sort(lSorted);
		
		StringBuilder sb = new StringBuilder();
		for(Symbol symbol: lSorted) {
			sb.append(symbol.getRepresentation()).append(" ");
		}
		return sb.toString().trim();
	}
	
	public int compareTo(Symbol symbol) {
		return this.m_ordinal - symbol.m_ordinal;
	}	
}
