package puzzle;

import java.util.Scanner;
import java.util.stream.Collectors;

import grid.GridLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class InitialGridContentProvider {

	public static InitialGridContentProvider fromFile(String fileName) {		
		InitialGridContentProvider igcp = null;
		File f = new File(fileName);
		
		if(!(f.exists() && f.isFile() && f.canRead())) {
			System.err.println("Unable to read file " + fileName);
		}
		else {
			
			List<String> lines = new ArrayList<>();			
			try {
				Scanner sc = new Scanner(f);
				while(sc.hasNextLine()) {
					lines.add(sc.nextLine());
				}
				sc.close();				
				igcp = new InitialGridContentProvider("File " + fileName, lines);
			} catch (FileNotFoundException e) {
				System.err.println("Failure reading file " + fileName + " : " + e.getMessage());
				igcp = null;
			}
		}
		
		return igcp;
	}
	
	public static InitialGridContentProvider fromArray(String[] a) {
		return new InitialGridContentProvider("Array of strings", Arrays.asList(a));
	}
	
	public static InitialGridContentProvider fromString(GridLayout layout, String s) {
		int gridSize = layout.m_rows;
		InitialGridContentProvider igcp = null;
		String noWhiteSpace = s.replaceAll("\\s+",  "");
		if(noWhiteSpace.length() == gridSize*gridSize)
		{
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
		return igcp;
	}
	
	// ----------------------------------------------------------------------------
	
	String m_sourceInfo;
	List<String> m_rawLines;
	List<String> m_dataLines;
	Set<String> m_symbolsUsed;
	
	private InitialGridContentProvider(String sourceInfo, List<String> rawLines) {
		m_sourceInfo = sourceInfo;
		m_rawLines = new ArrayList<>(rawLines);
		m_dataLines = 
				rawLines.stream()
				.filter(line -> line.trim().length() > 0)			// Remove blank lines
				.filter(line -> line.trim().charAt(0) != '#')		// Remove lines starting with #, treated as comments
				.map(line -> line.replaceAll("\\s+", ""))			// Remove whitespace from the line	
				.collect(Collectors.toList());
		
		m_symbolsUsed = new TreeSet<>();
		for(String line : m_dataLines) {
			for(int i=0; i < line.length(); i++) {
				char c = line.charAt(i);
				if(c != '.') {
					m_symbolsUsed.add(line.charAt(i) + "");
				}
			}
		}
	}
}
