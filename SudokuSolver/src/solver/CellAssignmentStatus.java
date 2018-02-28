package solver;

public enum CellAssignmentStatus {
	CanBeAssigned, CellAlreadyAssigned, SymbolAlreadyRuledOut, SymbolAlreadyAssignedInRow, SymbolAlreadyAssignedInColumn, SymbolAlreadyAssignedInBox;
}
