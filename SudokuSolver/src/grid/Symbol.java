package grid;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Represents a letter/number used to fill in a grid cell. 
 * 
 * Each one belongs to a set of symbols which apply to a particular puzzle, each symbol having a different 'ordinal' value.
 */

public class Symbol implements Comparable<Symbol> {

	private String m_representation;
	private String m_setName;
	private int m_ordinal;
	
	public Symbol(String representation, String setName, int ordinal) {
		m_representation = representation;
		m_setName = setName;
		m_ordinal = ordinal;
	}

	public String getRepresentation() {
		return m_representation;
	}

	public int ordinal() {
		return m_ordinal;
	}
	
	// For debuggers only
	public String toString() {
		return m_representation;
	}

	// Implement comparable. Only makes sense for comparing two symbols from the same set (and so definitely having different ordinals)
	@Override
	public int compareTo(Symbol symbol) {
		int diff = m_setName.compareTo(symbol.m_setName);
		if(diff == 0) {
			diff = ordinal() - symbol.ordinal();
		}		
		return diff;
	}	

	/**
	 * Generates a string recording each symbol in a collection.
	 * 
	 * @param symbols A collection (e.g. a List or a Set) of symbols
	 * 
	 * @return Space-separated, ordered list of the symbols as a string 
	 */
	
	public static String symbolCollectionRepresentation(Collection<Symbol> symbols) {
		// Functional approach.
		return symbols.stream()
				.sorted()
				.map(s -> s.getRepresentation())
				.collect(Collectors.joining(" "));
	}	
}
