package grid;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Represents and defines sets of symbols to be used for a Sudoku puzzles of various sizes. 
 */

public class Symbols {

	// Predefined sets of symbols which can be used for a particular puzzle. 
	public static Symbols SYMBOLS_1_TO_6 = new Symbols("1-to-6", 	"1", "2", "3", "4", "5", "6");

	public static Symbols SYMBOLS_1_TO_9 = new Symbols("1-to-9", 	"1", "2", "3", "4", "5", "6", "7", "8", "9");
	
	public static Symbols SYMBOLS_A_TO_I = new Symbols("A-to-I", 	"A", "B", "C", "D", "E", "F", "G", "H", "I");
	
	public static Symbols SYMBOLS_A_TO_P = new Symbols("A-to-P", 	"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
																	"K", "L", "M", "N", "O", "P");
	
	public static Symbols SYMBOLS_A_TO_Y = new Symbols("A-to-Y", 	"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
																	"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
																	"U", "V", "W", "X", "Y");
	
	// -------------------------------------------------------------------
	
	private String m_setName;
	private Map<String, Symbol> m_symbolRepresentationMap;
	
	private Symbols(String setName, String ... symbols) {
		m_setName = setName;
		m_symbolRepresentationMap = new LinkedHashMap<>();
		int ordinal = 1; 
		for(String symbolRepresentation : symbols) {
			Symbol s = new Symbol(symbolRepresentation, m_setName, ordinal++);
			m_symbolRepresentationMap.put(symbolRepresentation, s);
		}		
	}
	
	/**
	 * Does a string represent a symbol in this set ?
	 * 
	 * @param representation The string to compare
	 * @return The Symbol with this representation, or null if not matched.
	 */
	public Symbol isKnownSymbol(String representation) {
		return m_symbolRepresentationMap.get(representation);
	}
	
	public int size() {
		return m_symbolRepresentationMap.size();
	}
	
	public Set<Symbol> symbolSet() {
		return new LinkedHashSet<>(m_symbolRepresentationMap.values());
	}
	
	public String getRepresentation() {		
		return "Symbols (" + size() + ") in set '" + m_setName + "' : " + Symbol.symbolCollectionRepresentation(symbolSet());
	}
	
	// For debugger only
	public String toString() {
		return getRepresentation();
	}
	
	/**
	 * Match a set of symbols used in an initial grid to one of our know symbol sets.
	 *  
	 * @param totalSymbolsInSet Number of symbols in the known symbol set
	 * @param symbolsUsed Which symbols are provided
	 * @return The set of symbols matching the criteria, or null if no match found
	 */
	public static Symbols matchSymbolSet(int totalSymbolsInSet, Set<String> symbolsUsed) {

		if(SYMBOLS_1_TO_6.matchesSymbolSet(totalSymbolsInSet, symbolsUsed)) return SYMBOLS_1_TO_6; 
		if(SYMBOLS_1_TO_9.matchesSymbolSet(totalSymbolsInSet, symbolsUsed)) return SYMBOLS_1_TO_9; 
		if(SYMBOLS_A_TO_I.matchesSymbolSet(totalSymbolsInSet, symbolsUsed)) return SYMBOLS_A_TO_I; 
		if(SYMBOLS_A_TO_P.matchesSymbolSet(totalSymbolsInSet, symbolsUsed)) return SYMBOLS_A_TO_P; 
		if(SYMBOLS_A_TO_Y.matchesSymbolSet(totalSymbolsInSet, symbolsUsed)) return SYMBOLS_A_TO_Y;
		
		return null;
	}

	private boolean matchesSymbolSet(int totalSymbolsInSet, Set<String> symbolsUsed) {
		boolean matches = totalSymbolsInSet == m_symbolRepresentationMap.size();
		if(matches) {
			for(String s : symbolsUsed) {
				if(isKnownSymbol(s) == null) {
					matches = false;
				}
			}
		}
		
		return matches;
	}
}