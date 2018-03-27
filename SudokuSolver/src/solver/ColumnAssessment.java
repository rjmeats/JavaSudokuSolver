package solver;

import grid.Column;
import grid.Symbols;

/**
 * Class to record the ongoing assessment of what cell/symbol assignments are possible for a particular column in a grid.
 */

class ColumnAssessment extends CellSetAssessment {

	ColumnAssessment(Column column, Symbols symbols) {
		super(column, symbols);
	}
}

