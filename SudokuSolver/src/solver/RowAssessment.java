package solver;

import grid.Row;
import puzzle.SymbolsToUse;

public class RowAssessment extends LinearCellSetAssessment {

	Row m_row;
	
	public RowAssessment(Row row, SymbolsToUse symbols) {
		super(row, symbols);
		m_row = row;
	}
}

