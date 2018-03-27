package solver;

import grid.Row;
import grid.Symbols;

/**
 * Class to record the ongoing assessment of what cell/symbol assignments are possible for a particular row in a grid.
 */

class RowAssessment extends CellSetAssessment {

	RowAssessment(Row row, Symbols symbols) {
		super(row, symbols);
	}
}

