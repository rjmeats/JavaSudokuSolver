package tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import grid.GridLayout;
import grid.Symbols;
import puzzle.InitialGridContentProvider;
import puzzle.Puzzle;

public class PuzzleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private boolean stringsMatchIgnoringWhitespace(String s1, String s2) {
		return (s1 != null) && 
			   (s2 != null) && 
			   s1.replaceAll("\\s+",  "").equals(s2.replaceAll("\\s+",  ""));
	}

	private static Puzzle.Status invokePuzzle(Symbols symbols, GridLayout layout, String content) {
		InitialGridContentProvider contentProvider = InitialGridContentProvider.fromString(layout.m_rows, content);
		return Puzzle.solvePuzzle(symbols, layout, contentProvider);		
	}

	
	@Test
	public final void testSolve1() {
		
		String content = 
				"4..  ...  3.."  +
				".6.  5.1  ..."  +
				".7.  .3.  ..5"  +
				
				"..1  .86  .3."  +
				"9..  1.5  4.."  +
				"...  .9.  .5."  +
				
				"...  ..9  ..."  +
				"3..  ...  12."  +
				"81.  .5.  ..7";

		String solution = 
				"4 5 8  9 7 2  3 1 6" + 
				"2 6 3  5 4 1  8 7 9" +
				"1 7 9  6 3 8  2 4 5" +

				"5 4 1  7 8 6  9 3 2" +
				"9 3 7  1 2 5  4 6 8" +
				"6 8 2  3 9 4  7 5 1" +

				"7 2 6  4 1 9  5 8 3" +
				"3 9 5  8 6 7  1 2 4" +
				"8 1 4  2 5 3  6 9 7";

		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertTrue(status.m_initialGridOK);
		assertTrue(status.m_solved);
		assertTrue(status.m_valid);
		assertTrue(stringsMatchIgnoringWhitespace(solution, status.m_finalGrid));
	}

	@Test
	public final void testSolve2() {
		
		String content =
				"...  ...  .6."  +
				"...  3..  28."  +
				"...  .15  ..9"  +

				".5.  .7.  .1."  +
				"..1  4..  ..7"  +
				"..4  ...  8.6"  +

				".8.  ..2  .7."  +
				"34.  6..  9.."  +
				"..5  .47  ...";

		String solution = 
				"4 1 3  2 9 8  7 6 5" + 
				"5 7 9  3 6 4  2 8 1" +
				"2 6 8  7 1 5  3 4 9" +

				"6 5 2  8 7 9  4 1 3" +
				"8 3 1  4 2 6  5 9 7" +
				"7 9 4  1 5 3  8 2 6" +

				"9 8 6  5 3 2  1 7 4" +
				"3 4 7  6 8 1  9 5 2" +
				"1 2 5  9 4 7  6 3 8"; 

		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertTrue(status.m_initialGridOK);
		assertTrue(status.m_solved);
		assertTrue(status.m_valid);
		assertTrue(stringsMatchIgnoringWhitespace(solution, status.m_finalGrid));
	}
	
	// Based on v hard ES 25/10/2018
	@Test
	public final void testSolve3() {
		
		String content =
				
				"3..    ..9    ..4" +
				"..9    4..    327" +
				"7.4    8..    95." +

				"5..    39.    24." +
				".43    265    79." +
				".9.    .84    5.3" +

				".1.    ..8    4.9" +
				"478    9..    ..5" +
				"935    64.    ..2";

		String solution = 
				
				"3 5 6  7 2 9  1 8 4" +
				"1 8 9  4 5 6  3 2 7" +
				"7 2 4  8 1 3  9 5 6" +
				
				"5 6 1  3 9 7  2 4 8" +
				"8 4 3  2 6 5  7 9 1" +
				"2 9 7  1 8 4  5 6 3" +
				
				"6 1 2  5 7 8  4 3 9" +
				"4 7 8  9 3 2  6 1 5" +
				"9 3 5  6 4 1  8 7 2";
				
		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertTrue(status.m_initialGridOK);
		assertTrue(status.m_solved);
		assertTrue(status.m_valid);
		assertTrue(stringsMatchIgnoringWhitespace(solution, status.m_finalGrid));
	}
		
	// Based on v v hard ES 27/10/2018 - includes a case where Method 4 using a restricted cell combination (added after the original Method 4, which just looked at
	// symbol-driven combinations).
	@Test
	public final void testSolve4() {
		
		String content =
				
				"725    ...    .3." +
				"..4    5.7    9.6" +
				"...    ..3    ..." +

				"5..    7..    6.." +
				"...    ...    ..." +
				"..1    ..2    ..8" +

				".5.    6..    ..." +
				"2.9    3.4    561" +
				".4.    ...    379";

		String solution = 
				
				"7 2 5  9 6 8  1 3 4" +
				"8 3 4  5 1 7  9 2 6" +
				"9 1 6  2 4 3  8 5 7" +
				
				"5 8 2  7 9 1  6 4 3" +
				"4 9 7  8 3 6  2 1 5" +
				"3 6 1  4 5 2  7 9 8" +
				
				"1 5 3  6 7 9  4 8 2" +
				"2 7 9  3 8 4  5 6 1" +
				"6 4 8  1 2 5  3 7 9";
				
		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertTrue(status.m_initialGridOK);
		assertTrue(status.m_solved);
		assertTrue(status.m_valid);
		assertTrue(stringsMatchIgnoringWhitespace(solution, status.m_finalGrid));
	}
		
	@Test
	public final void testSolve2Insolulable1() {
		
		String content =
				"8..    ..1    2.."  +			
				".75    ...    ..."  +			
				"...    .5.    .64"  +			

				"..7    ...    ..6"  +			
				"9..    7..    ..."  +			
				"52.    ..9    .47"  +			

				"231    ...    ..."  +			
				"..6    .2.    1.9"  +			
				"...    ...    ...";			
				
		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertTrue(status.m_initialGridOK);
		assertFalse(status.m_solved);
		assertTrue(status.m_valid);
	}	

	@Test
	public final void testSolveInvalidGrid1() {
		
		String content =
				"6 . .  . . .  . 6 ."  +
				". . .  3 . .  2 8 ."  +
				". . .  . 1 5  . . 9"  +

				". 5 .  . 7 .  . 1 ."  +
				". . 1  4 . .  . . 7"  +
				". . 4  . . .  8 . 6"  +

				". 8 .  . . 2  . 7 ."  +
				"3 4 .  6 . .  9 . ."  +
				". . 5  . 4 7  . . .";
				
		Puzzle.Status status = invokePuzzle(Symbols.SYMBOLS_1_TO_9, GridLayout.GRID9x9, content);
		assertFalse(status.m_initialGridOK);
		
		// Error message should mention cells involved [1,1] [8,1] 
		assertTrue(status.m_invalidDetails.indexOf("[1,1]") != -1);
		assertTrue(status.m_invalidDetails.indexOf("[8,1]") != -1);
		
		assertFalse(status.m_solved);
	}	
}
