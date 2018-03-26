package puzzle;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collections;

import grid.Symbol;

public class SymbolsToUse {

	public static SymbolsToUse SET_1_TO_9 = new SymbolsToUse("1-to-9", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	public static SymbolsToUse SET_A_TO_I = new SymbolsToUse("A-to-I", "A", "B", "C", "D", "E", "F", "G", "H", "I");
	public static SymbolsToUse SET_1_TO_6 = new SymbolsToUse("1-to-6", "1", "2", "3", "4", "5", "6");
	public static SymbolsToUse SET_A_TO_Y = new SymbolsToUse("A-to-Y", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
															 		   "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
															 		   "U", "V", "W", "X", "Y");
	
	private String m_setName;
	private Set<Symbol> m_symbols;
	
	private SymbolsToUse(String setName, String ... symbols) {
		m_setName = setName;
		Set<Symbol> set = new LinkedHashSet<>();
		int ordinal = 1; 
		for(String s : symbols) {
			set.add(new Symbol(s, m_setName, ordinal++));
		}		
		m_symbols = Collections.unmodifiableSet(set);
	}
	
	public Set<Symbol> getSymbolSet() {
		return m_symbols;
	}
	
	public int size() {
		return m_symbols.size();
	}
	
	public Symbol isKnownSymbol(String representation)
	{
		Symbol symbol = null;
		
		for(Symbol trySymbol : m_symbols) {
			if(trySymbol.getRepresentation().equals(representation)) {
				symbol = trySymbol;
				break;
			}
		}
		
		return symbol;
	}
	
	// For debugger only
	public String toString() {
		
		String s = "Symbols (" + m_symbols.size() + ") in set " + m_setName + " : ";
		for(Symbol symbol : m_symbols) {
			s += symbol.getRepresentation() + " ";
		}
		
		return s.trim();
	}
}