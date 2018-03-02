package solver;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import grid.Cell;
import grid.Row;
import grid.Column;
import grid.Box;
import grid.Grid9x9;

import puzzle.Assignment;
import puzzle.Symbol;
import puzzle.SymbolsToUse;

public class Solver {

	Grid9x9 m_grid;	
	SymbolsToUse m_symbols;
	
	List<RowAssessment> m_lRows;
	List<ColumnAssessment> m_lColumns;
	List<BoxAssessment> m_lBoxes;	
	List<CellAssessment> m_lCells;
	List<CellSetAssessment> m_lCellSets;
	
	HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	HashMap<Row, RowAssessment> m_rowAssessmentsMap;
	HashMap<Column, ColumnAssessment> m_columnAssessmentsMap;
	HashMap<Box, BoxAssessment> m_boxAssessmentsMap;
	
	public Solver(Grid9x9 grid, SymbolsToUse symbols) {
		m_grid = grid;
		m_symbols = symbols;

		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellSets = new ArrayList<>();
		m_lCells = new ArrayList<>();
		m_cellAssessmentsMap = new HashMap<>();
		m_rowAssessmentsMap = new HashMap<>();
		m_columnAssessmentsMap = new HashMap<>();
		m_boxAssessmentsMap = new HashMap<>();
		
		for(Row row : m_grid.getRows()) {
			RowAssessment assessment = new RowAssessment(row, symbols);
			m_lRows.add(assessment);
			m_rowAssessmentsMap.put(row, assessment);
		}
		
		for(Column column : m_grid.getColumns()) {
			ColumnAssessment assessment = new ColumnAssessment(column, symbols);
			m_lColumns.add(assessment);
			m_columnAssessmentsMap.put(column, assessment);
		}

		for(Box box: m_grid.getBoxes()) {
			BoxAssessment assessment = new BoxAssessment(box, symbols);
			m_lBoxes.add(assessment);
			m_boxAssessmentsMap.put(box, assessment);
		}

		m_lCellSets = new ArrayList<>(m_lRows);
		m_lCellSets.addAll(m_lColumns);
		m_lCellSets.addAll(m_lBoxes);
			
		for(Cell cell : m_grid.getCells()) {
			RowAssessment row = getRowAssessmentForCell(cell);
			ColumnAssessment column = getColumnAssessmentForCell(cell);
			BoxAssessment box = getBoxAssessmentForCell(cell);
			CellAssessment cellAssessment = new CellAssessment(cell, row, column, box, symbols);				
			m_lCells.add(cellAssessment);
			m_cellAssessmentsMap.put(cell, cellAssessment);
			row.addCell(cellAssessment);
			column.addCell(cellAssessment);
			box.addCell(cellAssessment);
		}		

		// Need to go through already-assigned given cells and do equivalent of Solver.applyGivenValueToCell processing to these cells to track initial state.
		for(CellAssessment ca : m_lCells) {
			Cell cell = ca.m_cell;
			if(cell.isAssigned()) {
				Assignment assignment = cell.getAssignment();
				spreadAssignmentImpact(ca, assignment);
			}
		}				
	}

	private static String s_divider = "-----------------------------------";

	public void printCellSets() {
		printCellSets(-1);
	}
	
	public void printCellSets(int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append("Cell sets "  + stepInfo);
		sb1.append("\r\n");
		
		for(CellSetAssessment cellset : m_lCellSets) {
			sb1.append(cellset.m_cellSet.getRepresentation() + " : " + cellset.getSymbolAssignmentSummary());
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}

	public void printGrid(CellContentDisplayer ccd) { printGrid(ccd, -1); }
	
	public void printGrid(CellContentDisplayer ccd, int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		int currentHorizontalBoxNumber = -1;
		int currentVerticalBoxNumber = -1;
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append(ccd.getHeading() + stepInfo);
		sb1.append("\r\n");
		
		for(int rowNumber = 0; rowNumber < m_lRows.size(); rowNumber++) {
			int boxNumber = m_grid.getBoxFromGridPosition(rowNumber, 0).getBoxNumber();
			if(boxNumber != currentVerticalBoxNumber) {
				sb1.append("\r\n\r\n");
				currentVerticalBoxNumber = boxNumber; 
			}

			for(int columnNumber = 0; columnNumber < m_lColumns.size(); columnNumber++) {
				boxNumber = m_grid.getBoxFromGridPosition(rowNumber, columnNumber).getBoxNumber();
				if(boxNumber != currentHorizontalBoxNumber) {
					sb1.append("    ");
					currentHorizontalBoxNumber = boxNumber;
				}

				Cell c = m_grid.getCellFromGridPosition(rowNumber, columnNumber);
				CellAssessment cell = this.getCellAssessmentForCell(c);
				boolean highlight = (cell.m_cell.isAssigned() && (cell.m_cell.getAssignment().getStepNumber() == stepNumber));
				String contents = ccd.getContent(cell, highlight);
				sb1.append(" " + contents + " ");					
			}
			
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}

	CellAssessment getCellAssessmentForCell(Cell cell) {
		return m_cellAssessmentsMap.get(cell);
	}
	
	RowAssessment getRowAssessmentForCell(Cell cell) {
		return m_rowAssessmentsMap.get(cell.getRow());
	}

	ColumnAssessment getColumnAssessmentForCell(Cell cell) {
		return m_columnAssessmentsMap.get(cell.getColumn());
	}

	BoxAssessment getBoxAssessmentForCell(Cell cell) {
		return m_boxAssessmentsMap.get(cell.getBox());
	}
	
	public boolean nextStep(int stepNumber) {
		boolean changedState = false;
		
		if(!changedState) {
			// Look through unassigned cell for cases where only one symbol is a possible assignment.
			for(CellAssessment cell : m_lCells) {
				if(!cell.m_cell.isAssigned()) {
					Assignment a = cell.hasAssignmentAvailable(stepNumber);
					if(a != null) {
						String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + cell.m_cell.getGridLocationString();
						System.out.println(s);
						System.out.println();
						makeAssignment(cell, a);
						changedState = true;
						break;
					}
				}
			}			
		}
		
		if(!changedState) {
			// Look through each row, column, box for an unassigned symbol which can only go in one cell
			for(CellSetAssessment set : m_lCellSets) {
				Assignment a = set.hasAssignmentAvailable(stepNumber);
				if(a != null) {
					String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + a.getCell().getGridLocationString() + " from cell set " + set.m_cellSet.getRepresentation();
					System.out.println(s);
					System.out.println();
					CellAssessment ca = getCellAssessmentForCell(a.getCell());
					makeAssignment(ca, a);
					changedState = true;
					break;
				}
			}
		}
		
		if(!changedState) {
			// Look through each box to see where a particular unresolved symbol can only appear in a specific row or column of the box.
			// Where this arises, we can rule-out the symbol from the other cells in the row or column which are not in the box.
			int stateChanges = 0;
			for(BoxAssessment box : m_lBoxes) {
				List<SymbolRestriction> lRestrictions = box.findRestrictedSymbols();
				if(lRestrictions != null) {
					for(SymbolRestriction restriction : lRestrictions) {
						boolean causedStateChange = restriction.m_rowOrColumn.ruleOutSymbolOutsideBox(restriction);
						if(causedStateChange) {
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState) {
			// Look through each column box to see where a particular unresolved symbol can only appear in a specific box.
			// Where this arises, we can rule-out the symbol from the other cells in the box which are not in the column.
			int stateChanges = 0;
			for(ColumnAssessment column : m_lColumns) {
				List<SymbolRestriction> lRestrictions = column.findRestrictedSymbols();
				if(lRestrictions != null) {
					for(SymbolRestriction restriction : lRestrictions) {
						boolean causedStateChange = restriction.m_box.ruleOutSymbolOutsideRowOrColumn(restriction);
						if(causedStateChange) {
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState) {
			// And the same again for rows ...
			int stateChanges = 0;
			for(RowAssessment row : m_lRows) {
				List<SymbolRestriction> lRestrictions = row.findRestrictedSymbols();
				if(lRestrictions != null) {
					for(SymbolRestriction restriction : lRestrictions) {
						boolean causedStateChange = restriction.m_box.ruleOutSymbolOutsideRowOrColumn(restriction);
						if(causedStateChange) {
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState)
		{
			// Where n symbols in a row/column/box can only be assigned to the same n cells, then these cells can't be assigned to any other symbols.
			int stateChanges = 0;
			for(CellSetAssessment set : m_lCellSets) {
				List<SymbolSetRestriction> lRestrictedSymbolSets = set.findRestrictedSymbolSets();
				if(lRestrictedSymbolSets != null) {
					for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {						
						for(CellAssessment cell : symbolSetRestriction.m_lCells) {
							boolean causedStateChange = cell.ruleOutAllExcept(symbolSetRestriction.m_lSymbols);
							if(causedStateChange) {
								stateChanges++;
							}
						}
					}

					for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {
						for(Symbol symbol : symbolSetRestriction.m_lSymbols) {
							boolean causedStateChange = symbolSetRestriction.m_cellSet.ruleOutAllCellsBut(symbol, symbolSetRestriction.m_lCells);
							if(causedStateChange) {
								stateChanges++;
							}
						}
						
						List<CellSetAssessment> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
						for(CellSetAssessment cset : lAffectedCellSets) {
							for(CellAssessment cell : symbolSetRestriction.m_lCells) {
								boolean causedStateChange = cset.ruleOutCellFromOtherSymbols(cell, symbolSetRestriction.m_lSymbols);
								if(causedStateChange) {
									stateChanges++;
								}
							}
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		return changedState;
	}

	void makeAssignment(CellAssessment ca, Assignment assignment) {
		CellAssignmentStatus status = CellAssignmentStatus.checkCellCanBeAssigned(ca, assignment);
		if(status == CellAssignmentStatus.CanBeAssigned) {
			ca.m_cell.assign(assignment);

			// ???? Duplicated code
			spreadAssignmentImpact(ca, assignment);
		}
		else {
			System.err.println("Unexpected assignment failure : " + status.name() + " : " + assignment.toString());
		}					
	}

	void spreadAssignmentImpact(CellAssessment ca, Assignment assignment) {
		ca.assignmentMade(assignment.getSymbol());
		ca.getRow().assignmentMade(assignment, ca);
		ca.getColumn().assignmentMade(assignment, ca);
		ca.getBox().assignmentMade(assignment, ca);
	}
}
