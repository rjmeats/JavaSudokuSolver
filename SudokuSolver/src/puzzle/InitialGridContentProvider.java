package puzzle;

import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class InitialGridContentProvider {

	static InitialGridContentProvider fromFile(String fileName) {		
		List<String> lines = null;		
		File f = new File(fileName);
		
		if(!(f.exists() && f.isFile() && f.canRead())) {
			System.err.println("Unable to read file " + fileName);
		}
		else {
			
			lines = new ArrayList<>();
			
			try {
				Scanner sc = new Scanner(f);
				while(sc.hasNextLine()) {
					lines.add(sc.nextLine());
				}
				sc.close();
			} catch (FileNotFoundException e) {
				System.err.println("Unable to read file " + fileName);
				lines = null;
			}
		}
		
		InitialGridContentProvider h = (lines == null) ? null : new InitialGridContentProvider(lines);
		return h;
	}
	
	static InitialGridContentProvider fromArray(String[] a) {
		List<String> lines = Arrays.asList(a);		
		InitialGridContentProvider h = new InitialGridContentProvider(lines);
		return h;
	}
	
	// ----------------------------------------------------------------------------
	
	List<String> m_rawLines;
	List<String> m_dataLines;
	
	InitialGridContentProvider(List<String> rawLines) {
		m_rawLines = new ArrayList<>(rawLines);
		m_dataLines = new ArrayList<>();
		
		List<String> gridRows = removeCommentLines(removeBlankLines(rawLines));	// ???? Stream/filter
		
		for(int rowNumber = 0; rowNumber < gridRows.size(); rowNumber ++) {
			String rowString = gridRows.get(rowNumber);
			String despacedRowString = removeSpacing(rowString);
			m_dataLines.add(despacedRowString);
		}
	}
	
	// Ignore blank lines in the strings forming the initial grid
	private static List<String> removeBlankLines(List<String> l) {
		List<String> lOut = new ArrayList<>();
		for(String s : l) {
			if(s.trim().length() > 0) {
				lOut.add(s);
			}
		}
		
		return lOut;
	}

	// Ignore comment lines in the strings forming the initial grid		
	private static List<String> removeCommentLines(List<String> l) {
		char commentChar = '#';		
		List<String> lOut = new ArrayList<>();
		for(String s : l) {
			String trimS = s.trim();
			if(trimS.length() > 0 && trimS.charAt(0) != commentChar) {
				lOut.add(s);
			}
		}
		
		return lOut;
	}

	private static String removeSpacing(String rowString) {
		return rowString.replaceAll("\\s+", "");
	}	
}
