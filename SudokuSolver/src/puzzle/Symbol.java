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

	static boolean compareSymbolLists(List<Symbol> lSymbols1, List<Symbol> lSymbols2)
	{
		boolean same = true;

		if(lSymbols1.size() != lSymbols2.size()) return false;
		
		List<Symbol> l1 = new ArrayList<>(lSymbols1);
		List<Symbol> l2 = new ArrayList<>(lSymbols2);
		
		Collections.sort(l1);
		Collections.sort(l2);

		for(int n=0; n < l1.size(); n++) {
			if(l1.get(n) != l2.get(n)) {
				same = false;
				break;
			}
		}
		
		return same;
	}
}
