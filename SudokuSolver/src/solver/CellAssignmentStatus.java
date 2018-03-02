package solver;

import puzzle.Assignment;
import puzzle.Symbol;

public enum CellAssignmentStatus {
	CanBeAssigned, CellAlreadyAssigned, SymbolAlreadyRuledOutForCell, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;

	static CellAssignmentStatus checkCellCanBeAssigned(CellAssessment cell, Assignment assignment) {
		Symbol symbol = assignment.getSymbol();
		CellAssignmentStatus status = CellAssignmentStatus.CanBeAssigned;
		
		if(cell.m_cell.isAssigned()) {
			status = CellAssignmentStatus.CellAlreadyAssigned;
		}
		else if(!cell.couldBe(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOutForCell;			
		}
		else if(cell.isRuledOut(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyRuledOutForCell;			
		}
		else if(cell.getRow().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInRow;
		}
		else if(cell.getColumn().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInColumn;			
		}
		else if(cell.getBox().symbolAlreadyAssigned(symbol)) {
			status = CellAssignmentStatus.SymbolAlreadyAssignedInBox;			
		}

		return status;
	}
	

}
