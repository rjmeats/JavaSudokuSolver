package puzzle;

import java.util.Scanner;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import grid.GridLayout;
import grid.Symbols;

/**
 * Provides initial Sudoku grid information from different text sources (file, String, Array)
 * 
 * Whitespace is ignored. For some sources, comment lines are permitted (beginning with '#') which are also ignored.
 * Each source should provide a single-character value for each cell in the grid, using '.' for the unknown values.
 */
public class InitialGridContentProvider {

	public final static char UNKNOWN_CELL_VALUE = '.';
	public final static char COMMENT_INDICATOR = '#';
	
	/**
	 * Factory, reading the initial Sudoku grid information from a file, ignoring comment lines.
	 * 
	 * @param fileName File to read
	 * @return The InitialGridContentProvider created, or null if there was a problem
	 */
	public static InitialGridContentProvider fromFile(String fileName) {		
		InitialGridContentProvider igcp = null;
		File f = new File(fileName);
		
		// Check the file is readable
		if(!(f.exists() && f.isFile() && f.canRead())) {
			System.err.println("Unable to read file " + fileName);
		}
		else {			
			// Read each line from the file into an array, and use the array to create the Content Provider object
			List<String> lines = new ArrayList<>();			
			try (Scanner sc = new Scanner(f)) {
				while(sc.hasNextLine()) {
					lines.add(sc.nextLine());
				}
			} catch (FileNotFoundException e) {
				System.err.println("Failure reading file " + fileName + " : " + e.getMessage());
				lines = null;
			}
			
			if(lines != null) {
				igcp = new InitialGridContentProvider("File " + fileName, lines);
			}
		}
		
		return igcp;
	}
	
	/**
	 * Factory, reading the initial Sudoku grid information from an array of strings, ignoring comment lines.
	 * 
	 * @param a Array of strings, one per line, forming the initial grid
	 * @return The InitialGridContentProvider created, or null if there was a problem
	 */
	public static InitialGridContentProvider fromArray(String[] a) {
		// Convert the array to a list and then create the Content Provider
		return new InitialGridContentProvider("Array of strings", Arrays.asList(a));
	}
	
	/**
	 * Factory, reading the initial Sudoku grid information from a single string - comments are not allowed.
	 * 
	 * The string is split into lines based on the grid size specified (applied after whitespace is removed).
	 * After whitespace is removed, the string should have a character for each grid cell.
	 * 
	 * @param gridSize How many rows/columns are in the grid
	 * @param s String containing the grid contents
	 * @return The InitialGridContentProvider created, or null if there was a problem
	 */
	public static InitialGridContentProvider fromString(int gridSize, String s) {
		InitialGridContentProvider igcp = null;
		String noWhiteSpace = s.replaceAll("\\s+",  "");
		
		// After removing white space, the string must have exactly one character per cell, so
		// that we can split it into lines before creating the Content Provider object
		if(noWhiteSpace.length() == gridSize*gridSize) {
			List<String> lines = new ArrayList<>();			
			for(int lineNo = 0; lineNo < gridSize; lineNo++) {
				String line = "";
				for(int columnNo = 0; columnNo < gridSize; columnNo++) {
					int index = gridSize*lineNo+columnNo;
					line += noWhiteSpace.charAt(index);
				}
				lines.add(line);
			}
			igcp = new InitialGridContentProvider("String", lines);
		}
		else {
			System.err.println("Grid string length without whitespace (" + noWhiteSpace.length() + ") does not match the expected grid size " + gridSize*gridSize);
		}
		return igcp;
	}
	
	// ----------------------------------------------------------------------------
	
	private String m_sourceInfo;			// Where did the content come from
	private List<String> m_rawLines;		// The text lines used to create the Content Provider
	private List<String> m_dataLines;		// The text lines after processing to remove comments and whitespace
	private Set<String> m_symbolsUsed;		// What symbols have been specified in the lines ?
	
	private InitialGridContentProvider(String sourceInfo, List<String> rawLines) {
		m_sourceInfo = sourceInfo;
		m_rawLines = new ArrayList<>(rawLines);
		m_dataLines = 
				rawLines.stream()
				.filter(line -> line.trim().length() > 0)						// Remove blank lines
				.filter(line -> line.trim().charAt(0) != COMMENT_INDICATOR)		// Remove comment lines
				.map(line -> line.replaceAll("\\s+", ""))						// Remove whitespace from the line	
				.collect(Collectors.toList());

		// Generate an ordered set of the symbols used for the initial 'given' cells.
		m_symbolsUsed = new TreeSet<>();
		for(String line : m_dataLines) {
			for(int i=0; i < line.length(); i++) {
				char c = line.charAt(i);
				if(c != UNKNOWN_CELL_VALUE) {
					m_symbolsUsed.add(c + "");
				}
			}
		}
	}
	
	String sourceInfo() {
		return m_sourceInfo;
	}

	List<String> rawLines() {
		return new ArrayList<>(m_rawLines);
	}

	List<String> dataLines() {
		return new ArrayList<>(m_dataLines);
	}

	/**
	 * Option to try to work out the GridLayout associated with this content
	 * 
	 * @return The layout determined, or null if not recognised/invalid.
	 */
	public GridLayout workOutGridLayout() {
		int gridRows = m_dataLines.size();
		boolean foundError = false;
		GridLayout layout = GridLayout.getGridLayoutOfSize(gridRows);
		if(layout == null) {
			System.err.println("Grid layout not recognised - rows=" + gridRows);
			foundError = true;
		}
		else {
			// Check that each row has the expected number of columns for a square grid
			int rowNumber = 0;
			for(String row : m_dataLines) {
				if(row.length() != gridRows) {
					System.err.println("Grid layout - number of columns (" + row.length()+ ") is not equal to number of rows (" + gridRows + ") " + 
									   "on row " + (rowNumber+1) + ": [" + row + "]");
					foundError = true;					
				}
				rowNumber++;
			}
		}
						
		return foundError ? null : layout;
	}
	
	/**
	 * Option to try to work out what set of symbols are associated with this content
	 * 
	 * @return The symbols determined, or null if not recognised/invalid.
	 */
	public Symbols workOutSymbolsToUse() {
		int gridRows = m_dataLines.size();
		boolean foundError = false;
		Symbols symbolsToUse = null;

		// The number of symbols used must be no more than the number of rows (or columns)
		if(m_symbolsUsed.size() > gridRows) {
			System.err.println("Too many different symbols used (" + m_symbolsUsed.size() + ") for this grid size (" + gridRows + ")");
			System.err.println("Symbols used: " + m_symbolsUsed.toString());
			foundError = true;								
		}
		else {
			// And the symbols used must belong to one of our known sets of the appropriate size.
			symbolsToUse = Symbols.matchSymbolSet(gridRows, m_symbolsUsed);
			if(symbolsToUse == null) {
				System.err.println("Symbols used in the initial grid are not from a recognised set for this grid size: " + m_symbolsUsed.toString());
				foundError = true;
			}
		}
		
		return foundError ? null : symbolsToUse;
	}
}
