package solver;

import grid.Column;
import puzzle.SymbolsToUse;

class ColumnAssessment extends LinearCellSetAssessment {

	Column m_column;
	
	public ColumnAssessment(Column column, SymbolsToUse symbols) {
		super(column, symbols);
		m_column = column;
	}
}

