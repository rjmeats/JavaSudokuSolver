package solver;

import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import grid.Cell;
import grid.CellSet;
import grid.Row;
import grid.Column;
import grid.Box;
import grid.Grid9x9;

import puzzle.Assignment;
import puzzle.AssignmentMethod;
import puzzle.Symbol;
import puzzle.SymbolsToUse;
import diagnostics.FormatUtils;
import diagnostics.GridFormatter;

public class Solver {

	Grid9x9 m_grid;	
	SymbolsToUse m_symbols;
	
	List<RowAssessment> m_lRows;
	List<ColumnAssessment> m_lColumns;
	List<BoxAssessment> m_lBoxes;	
	List<CellAssessment> m_lCellAssessments;
	List<CellSetAssessment> m_lCellSets;
	
	HashMap<Cell, CellAssessment> m_cellAssessmentsMap;
	HashMap<Row, RowAssessment> m_rowAssessmentsMap;
	HashMap<Column, ColumnAssessment> m_columnAssessmentsMap;
	HashMap<Box, BoxAssessment> m_boxAssessmentsMap;
	HashMap<CellSet, CellSetAssessment> m_cellSetAssessmentsMap;
	
	public Solver(Grid9x9 grid, SymbolsToUse symbols) {
		m_grid = grid;
		m_symbols = symbols;

		m_lRows = new ArrayList<>();
		m_lColumns = new ArrayList<>();
		m_lBoxes = new ArrayList<>();
		m_lCellAssessments = new ArrayList<>();
		m_cellAssessmentsMap = new HashMap<>();
		m_rowAssessmentsMap = new HashMap<>();
		m_columnAssessmentsMap = new HashMap<>();
		m_boxAssessmentsMap = new HashMap<>();
		
		for(Row row : m_grid.rows()) {
			RowAssessment assessment = new RowAssessment(row, symbols);
			m_lRows.add(assessment);
			m_rowAssessmentsMap.put(row, assessment);
		}
		
		for(Column column : m_grid.columns()) {
			ColumnAssessment assessment = new ColumnAssessment(column, symbols);
			m_lColumns.add(assessment);
			m_columnAssessmentsMap.put(column, assessment);
		}

		for(Box box: m_grid.boxes()) {
			BoxAssessment assessment = new BoxAssessment(box, symbols);
			m_lBoxes.add(assessment);
			m_boxAssessmentsMap.put(box, assessment);
		}

		m_lCellSets = new ArrayList<>(m_lRows);
		m_lCellSets.addAll(m_lColumns);
		m_lCellSets.addAll(m_lBoxes);
		
		m_cellSetAssessmentsMap = new HashMap<>(m_rowAssessmentsMap);
		m_cellSetAssessmentsMap.putAll(m_columnAssessmentsMap);
		m_cellSetAssessmentsMap.putAll(m_boxAssessmentsMap);
			
		for(Cell cell : m_grid.cells()) {
			setUpCellAssessment(cell);
		}		

		// Need to go through already-assigned given cells and do equivalent of Solver.applyGivenValueToCell processing to these cells to track initial state.
		for(CellAssessment ca : m_lCellAssessments) {
			Cell cell = ca.cell();
			if(cell.isAssigned()) {
				Assignment assignment = cell.getAssignment();
				spreadAssignmentImpact(ca, assignment);
			}
		}				
	}

	private void setUpCellAssessment(Cell cell) {
		RowAssessment row = getRowAssessmentForCell(cell);
		ColumnAssessment column = getColumnAssessmentForCell(cell);
		BoxAssessment box = getBoxAssessmentForCell(cell);
		CellAssessment cellAssessment = new CellAssessment(cell, row, column, box, m_symbols);				
		m_lCellAssessments.add(cellAssessment);
		m_cellAssessmentsMap.put(cell, cellAssessment);
		row.addCell(cell);
		column.addCell(cell);
		box.addCell(cell);		
	}
	
	
	CellAssessment getCellAssessmentForCell(Cell cell) {
		return m_cellAssessmentsMap.get(cell);
	}
	
	CellSetAssessment getCellSetAssessmentForCellSet(CellSet cellSet) {
		return m_cellSetAssessmentsMap.get(cellSet);
	}
	
	BoxAssessment getBoxAssessmentForBox(Box box) {
		return (BoxAssessment)m_cellSetAssessmentsMap.get(box);
	}
	
	RowAssessment getRowAssessmentForCell(Cell cell) {
		return m_rowAssessmentsMap.get(cell.row());
	}

	ColumnAssessment getColumnAssessmentForCell(Cell cell) {
		return m_columnAssessmentsMap.get(cell.column());
	}

	BoxAssessment getBoxAssessmentForCell(Cell cell) {
		return m_boxAssessmentsMap.get(cell.box());
	}
	
	public boolean nextStep(int stepNumber) {
		boolean changedState = false;
		
		if(!changedState) {
			// Look through unassigned cell for cases where only one symbol is a possible assignment.
			for(CellAssessment ca : m_lCellAssessments) {
				if(!ca.isAssigned()) {
					Assignment a = hasAssignmentAvailable(ca, stepNumber);
					if(a != null) {
						String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + ca.cell().getOneBasedGridLocationString();
						System.out.println(s);
						System.out.println();
						makeAssignment(ca, a);
						changedState = true;
						break;
					}
				}
			}			
		}
		
		if(!changedState) {
			// Look through each row, column, box for an unassigned symbol which can only go in one cell
			for(CellSetAssessment set : m_lCellSets) {
				Assignment a = hasAssignmentAvailable(set, stepNumber);
				if(a != null) {
					String s = "Assigned symbol " + a.getSymbol().toString() + " to cell " + a.getCell().getOneBasedGridLocationString() + " from cell set " + set.getRepresentation();
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
						CellSetAssessment csa = getCellSetAssessmentForCellSet(restriction.m_rowOrColumn);
						boolean causedStateChange = ruleOutSymbolOutsideBox(csa, restriction);
						if(causedStateChange) {
							stateChanges++;
						}
					}
				}
			}
			
			changedState = (stateChanges > 0);
		}
		
		if(!changedState) {
			// Look through each column to see where a particular unresolved symbol can only appear in a specific box.
			// Where this arises, we can rule-out the symbol from the other cells in the box which are not in the column.
			int stateChanges = 0;
			for(ColumnAssessment column : m_lColumns) {
				List<SymbolRestriction> lRestrictions = column.findRestrictedSymbols();
				if(lRestrictions != null) {
					for(SymbolRestriction restriction : lRestrictions) {
						BoxAssessment boxa = getBoxAssessmentForBox(restriction.m_box);
						boolean causedStateChange = ruleOutSymbolOutsideRowOrColumn(boxa, restriction);
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
						BoxAssessment boxa = getBoxAssessmentForBox(restriction.m_box);
						boolean causedStateChange = ruleOutSymbolOutsideRowOrColumn(boxa, restriction);
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
						for(Cell cell : symbolSetRestriction.m_lCells) {
							CellAssessment ca = getCellAssessmentForCell(cell);
							boolean causedStateChange = ca.ruleOutAllSymbolsExcept(symbolSetRestriction.m_lSymbols);
							if(causedStateChange) {
								stateChanges++;
							}
						}
					}

					for(SymbolSetRestriction symbolSetRestriction : lRestrictedSymbolSets) {
						for(Symbol symbol : symbolSetRestriction.m_lSymbols) {
							CellSetAssessment cseta = getCellSetAssessmentForCellSet(symbolSetRestriction.m_cellSet);
							int causedChange = cseta.ruleOutAllOtherCellsForSymbol(symbolSetRestriction.m_lCells, symbol);
							if(causedChange > 0) {
								stateChanges++;
							}
						}
						
						List<CellSet> lAffectedCellSets = symbolSetRestriction.getAffectedCellSets();
						for(CellSet cset : lAffectedCellSets) {
							CellSetAssessment cseta = getCellSetAssessmentForCellSet(cset);
							for(Cell cell : symbolSetRestriction.m_lCells) {
								int causedChange = cseta.ruleOutCellFromOtherSymbols(cell, symbolSetRestriction.m_lSymbols);
								if(causedChange > 0) {
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

	// If there is only one symbol which can still be assigned to this cell, then we have an assignment 
	Assignment hasAssignmentAvailable(CellAssessment ca, int stepNumber) {
		Assignment a = null;
		if(!ca.isAssigned() && ca.couldBeCount() == 1) {
			Symbol symbol = ca.getCouldBeSymbols().stream().findFirst().get();
			a = new Assignment(ca.cell(), symbol, AssignmentMethod.AutomatedDeduction, "Only symbol still possible for cell", stepNumber);
		}		
		return a;
	}
	
	Assignment hasAssignmentAvailable(CellSetAssessment ca, int stepNumber) {
		Assignment assignableCell = null;
		for(Symbol symbol : m_symbols.getSymbolSet()) {
			if(!ca.symbolAlreadyAssigned(symbol)) {
				List<Cell> lCells = ca.getCouldBeCellsForSymbol(symbol);
				if(lCells.size() == 1) {
					assignableCell = new Assignment(lCells.get(0), symbol, AssignmentMethod.AutomatedDeduction, "Only cell for symbol in " + ca.getRepresentation(), stepNumber);
					break;
				}
			}
		}
		
		return assignableCell;
	}

	void makeAssignment(CellAssessment ca, Assignment assignment) {
		CellAssignmentStatus status = CellAssignmentStatus.checkCellCanBeAssigned(ca, assignment);
		if(status == CellAssignmentStatus.CanBeAssigned) {
			ca.cell().assign(assignment);
			spreadAssignmentImpact(ca, assignment);
		}
		else {
			System.err.println("Unexpected assignment failure : " + status.name() + " : " + assignment.toString());
		}					
	}

	// Can be called for an initial 'given' assignment, or for one deduced
	void spreadAssignmentImpact(CellAssessment ca, Assignment assignment) {
		ca.assignmentMade(assignment.getSymbol());		
		for(CellSetAssessment csa : ca.getCellSetAssessments()) {
			spreadAssignmentImpact(csa, ca, assignment);			
		}
	}
	
	void spreadAssignmentImpact(CellSetAssessment csa, CellAssessment ca, Assignment assignment) {
		Symbol symbol = assignment.getSymbol();
		Cell assignmentCell = assignment.getCell();

		// A cell in this cell-set has had an assignment made. Update the cell-set record to reflect this 
		csa.assignmentMade(assignment.getSymbol(), assignmentCell);
	
		// Go through the other cells in this cell-set, and rule out this symbol from being assigned to those cells 
		for(Cell otherCell : csa.getCellSet().getCells()) {
			if(otherCell != assignmentCell) {
				// This cell isn't assigned to the symbol
				CellAssessment otherCellInCellSet = getCellAssessmentForCell(otherCell);
				otherCellInCellSet.ruleOutSymbol(symbol);
				// And update all the cell sets in which this other cell resides to reflect that the symbol is not in this cell
				// NB One of these == csa, but it should be harmless to repeat the ruling out
				for(CellSetAssessment csaOfOtherCell : otherCellInCellSet.getCellSetAssessments()) {
					csaOfOtherCell.ruleOutCellForSymbol(otherCell, symbol);
				}				
			}
		}		
	}	

	boolean ruleOutSymbolOutsideBox(CellSetAssessment csa, SymbolRestriction restriction) {
		boolean changedState = false;
		// For cells not in the restriction box, rule out the symbol.
		for(Cell cell : csa.getCellSet().getCells()) {
			if(!restriction.m_box.containsCell(cell)) {
				CellAssessment ca = getCellAssessmentForCell(cell);
				if(!ca.isRuledOut(restriction.m_symbol)) {
//System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getGridLocationString());				
					ca.ruleOutSymbol(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}
	
	boolean ruleOutSymbolOutsideRowOrColumn(CellSetAssessment boxcsa, SymbolRestriction restriction) {
		boolean changedState = false;
		// For cells not in the restriction row/column, rule out the symbol.
		for(Cell cell : boxcsa.getCellSet().getCells()) {
			if(!restriction.m_rowOrColumn.containsCell(cell)) {
				CellAssessment ca = getCellAssessmentForCell(cell);
				if(!ca.isRuledOut(restriction.m_symbol)) {
//System.err.println("Ruling out symbol " + restriction.m_symbol.toString() + " for cell " + cell.getGridLocationString());				
					ca.ruleOutSymbol(restriction.m_symbol);
					changedState = true;
				}
			}
		}
		
		return changedState;
	}


	// --------------------------------------------------------------------------------
	
	
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
			sb1.append(cellset.getRepresentation() + " : " + cellset.getSymbolAssignmentSummary());
			sb1.append("\r\n");
		}
		
		System.out.println(sb1.toString());
	}

	public void printGrid(CellContentProvider ccd) { printGrid(ccd, -1); }
	
	public void printGrid(CellContentProvider ccd, int stepNumber) {
		StringBuilder sb1 = new StringBuilder();
		
		String stepInfo = stepNumber < 0 ? "" : " - step " + stepNumber;
		
		sb1.append("\r\n").append(s_divider).append("\r\n\r\n");
		sb1.append(ccd.getHeading() + stepInfo);
		sb1.append("\r\n");

		GridFormatter gf = new GridFormatter(m_grid);
		sb1.append(gf.formatGrid(ccd, stepNumber));
		System.out.println(sb1.toString());
	}

	// ==============================================================
	
	public class CouldBeValueCountDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be-value' count: ~ => Given  = => Assigned  * => Could be assigned"; }
		
		public String getContent(Cell cell) {
			CellAssessment ca = getCellAssessmentForCell(cell);
			String representation = "" + ca.couldBeCount();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (cell.isAssigned() && cell.getAssignment().getMethod() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(cell.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 5));
		}
	}
	
	public class CouldBeValueDisplay implements CellContentProvider {
		
		public String getHeading() { return "Cell 'Could-be' values"; }
		
		public String getContent(Cell cell) {
			CellAssessment ca = getCellAssessmentForCell(cell);
			String representation = "" + ca.toCouldBeSymbolsString();
			if(!cell.isAssigned() && ca.couldBeCount() == 1) {
				representation = "*"+ representation;
			}
			else if (cell.isAssigned() && cell.getAssignment().getMethod() == AssignmentMethod.Given) {
				representation = "~" + representation;				
			}
			else if(cell.isAssigned()) {
				representation = "=" + representation;								
			}
			return(FormatUtils.padRight(representation, 17));
		}
	}

}
