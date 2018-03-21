package grid;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Symbol implements Comparable<Symbol> {

	private String m_representation;
	private int m_ordinal;
	
	public Symbol(String representation, int ordinal) {
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
	
	@Override
	public int compareTo(Symbol symbol) {
		return this.m_ordinal - symbol.m_ordinal;
	}	

	public static String symbolCollectionToString(Collection<Symbol> l)
	{
		List<Symbol> lSorted = new ArrayList<>(l);
		Collections.sort(lSorted);		
		StringBuilder sb = new StringBuilder();
		for(Symbol symbol: lSorted) {
			sb.append(symbol.getRepresentation()).append(" ");
		}
		return sb.toString().trim();
	}	
}
