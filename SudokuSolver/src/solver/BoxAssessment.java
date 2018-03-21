package solver;

import java.util.Set;

import grid.Cell;
import grid.CellSet;
import grid.Box;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

public class BoxAssessment extends CellSetAssessment {

	Box m_box;
	
	public BoxAssessment(Box box, SymbolsToUse symbols) {
		super(box, symbols);
		m_box = box;
	}
}

class SymbolRestriction {
	Symbol m_symbol;
	CellSet m_restrictorCellSet;
	CellSet m_restrictedCellSet;
	Set<Cell> m_restrictedCells;
	
	SymbolRestriction(Symbol symbol, CellSet restrictor, CellSet restricted) {
		m_symbol = symbol;
		m_restrictorCellSet = restrictor;
		m_restrictedCellSet = restricted;
		m_restrictedCells = m_restrictedCellSet.getCellsNotIn(m_restrictorCellSet);
	}
	
	String getRepresentation() {
		return "Symbol " + m_symbol.getRepresentation() + " in " + m_restrictorCellSet.getRepresentation() + 
					" restricted to " + m_restrictedCellSet.getRepresentation() + " : symbol cannot be present in cells: " + Cell.cellCollectionToString(m_restrictedCells);
	}
}
