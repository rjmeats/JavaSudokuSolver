package puzzle;

import java.util.Set;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.ArrayList;

public class SymbolsToUse {

	public static SymbolsToUse SET_1_TO_9 = new SymbolsToUse("1", "2", "3", "4", "5", "6", "7", "8", "9");
	public static SymbolsToUse SET_A_TO_I = new SymbolsToUse("A", "B", "C", "D", "E", "F", "G", "H", "I");
	
	private Set<Symbol> m_symbols;
	
	SymbolsToUse(String ... symbols) {
		m_symbols = new LinkedHashSet<>();
		int ordinal = 1; 
		for(String s : symbols) {
			Symbol symbol = new Symbol(s, ordinal++);
			m_symbols.add(symbol);
		}
	}
	
	public List<Symbol> getSymbolList() {
		return new ArrayList<Symbol>(m_symbols);
	}
	
	public int size() {
		return m_symbols.size();
	}
	
	public Symbol isKnownSymbol(String representation)
	{
		Symbol symbol = null;
		
		for(Symbol trySymbol : m_symbols)
		{
			if(trySymbol.getRepresentation().equals(representation))
			{
				symbol = trySymbol;
				break;
			}
		}
		
		return symbol;
	}
	
	public String toString() {
		
		String s = "Symbols (" + m_symbols.size() + ") in set: ";
		for(Symbol symbol : m_symbols) {
			s += symbol.getRepresentation() + " ";
		}
		
		return s.trim();
	}
}