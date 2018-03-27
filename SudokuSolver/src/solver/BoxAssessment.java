package solver;

import grid.Box;
import grid.Symbols;

/**
 * Class to record the ongoing assessment of what cell/symbol assignments are possible for a particular box in a grid.
 */

class BoxAssessment extends CellSetAssessment {

	BoxAssessment(Box box, Symbols symbols) {
		super(box, symbols);
	}
}
