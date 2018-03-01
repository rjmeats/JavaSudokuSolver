package puzzle;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class InitialGridContentProvider {

	static InitialGridContentProvider fromFile(String fileName) {		
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
				igcp = new InitialGridContentProvider(lines);
			} catch (FileNotFoundException e) {
				System.err.println("Failure reading file " + fileName + " : " + e.getMessage());
				igcp = null;
			}
		}
		
		return igcp;
	}
	
	static InitialGridContentProvider fromArray(String[] a) {
		return new InitialGridContentProvider(Arrays.asList(a));
	}
	
	// ----------------------------------------------------------------------------
	
	List<String> m_rawLines;
	List<String> m_dataLines;
	
	InitialGridContentProvider(List<String> rawLines) {
		m_rawLines = new ArrayList<>(rawLines);
		m_dataLines = 
				rawLines.stream()
				.filter(line -> line.trim().length() > 0)			// Remove blank lines
				.filter(line -> line.trim().charAt(0) != '#')		// Remove lines starting with #, treated as comments
				.map(line -> line.replaceAll("\\s+", ""))			// Remove whitespace from the line	
				.collect(Collectors.toList());
	}	
}
