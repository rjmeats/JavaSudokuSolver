package puzzle;

public class SampleSudokus {

	public static String[] s_initialValues1 = {
			
			"2..    .4.    .6.",			
			"74.    ..9    8.2",			
			".9.    682    73.",			

			".2.    .76    .4.",			
			"..1    .5.    3..",			
			".5.    29.    .7.",			

			".69    725    .1.",			
			"5.2    9..    .87",			
			".3.    .6.    ..5",			
	};

	public static String[] s_initialValues2 = {
		
		"..6    5.8    2..",			
		"5..    2.9    ..7",			
		"..2    .1.    4..",			

		"..3    ...    9..",			
		"...    6.2    ...",			
		"4.8    ...    5.1",			

		".3.    ...    .6.",			
		".5.    8.1    .7.",			
		"1..    ...    ..4",		
	};

	// www.sudokuwiki.org gets a bit further with some advanced strategies, but then stops. Apparently there are 149 solutions!
	public static String[] s_initialValuesLeMondeHard = {
		
		"8..    ..1    2..",			
		".75    ...    ...",			
		"...    .5.    .64",			

		"..7    ...    ..6",			
		"9..    7..    ...",			
		"52.    ..9    .47",			

		"231    ...    ...",			
		"..6    .2.    1.9",			
		"...    ...    ...",			
	};
	
	public static String[] s_initialValuesLeMondeHard2 = {
		
		"...    76.    ..8",			
		"9..    ...    ...",			
		"...    2.9    ...",			

		".5.    912    .46",			
		".9.    .4.    2.5",			
		"..6    .8.    .1.",			

		"...    .95    ..1",			
		".7.    ..6    ...",			
		"3.9    8..    4..",			
	};
	
	// https://puzzling.stackexchange.com/questions/37804/non-brute-force-sudoku
	public static String[] s_initialValuesStackOverflow = {
		
		"3..    .72    596",			
		"...    4..    ..2",			
		"..7    ...    3.4",			

		"...    ...    .4.",			
		"...    ...    ...",			
		".9.    ...    ...",			

		"8.4    ...    2..",			
		"9..    ..7    ...",			
		"736    24.    ..9",			
	};

	public static String[] s_initialValuesStackOverflow2 = {
		
		"3..    .72    596",			
		"1..    4..    ..2",			
		"..7    ...    3.4",			

		"...    ...    .4.",			
		"...    .8.    .5.",			
		".9.    ...    ...",			

		"8.4    ...    2..",			
		"9..    ..7    ...",			
		"736    24.    ..9",			
	};
	
	public static String[] s_1 = {
		
		"...    .4.    .52",			
		"..2    1.5    8.7",			
		"...    ...    .4.",			

		"6.8    4..    .9.",			
		"3.5    ...    ..8",			
		"...    ..9    .1.",			

		"57.    .23    .8.",			
		"..9    ...    ...",			
		"..6    .17    ...",			
	};
	
		// https://www.youtube.com/watch?v=myy7ldfgTnQ
	public static String[] s_times9636 = {
		
		"...    ..9    ...",			
		"...    .4.    ...",			
		"234    58.    ...",			

		"...    ...    ..1",			
		"76.    .9.    48.",			
		"39.    ...    5..",			

		".5.    ...    7..",			
		"..9    17.    8..",			
		"4..    93.    2..",			
	};
	
	// https://www.youtube.com/watch?v=4FlfjmmcjPs
	public static String[] s_times9633 = {
		
		"...    ..7   35.",			
		"...    .2.    19.",			
		"...    ..1    .2.",			

		"..6    .5.    ..3",			
		".83    ...    67.",			
		"7..    .6.    4..",			

		".6.    3..    ...",			
		".92    .8.    ...",			
		".54    6..    ...",			
	};
	
	// https://www.youtube.com/watch?v=o3PQrNecoag
	// Needs combinations to get going ...
	public static String[] s_hard = {
		
		"9..    ...    7..",			
		"..8    4.5    ...",			
		".5.    ..2    ..3",			

		"8..    .9.    ...",			
		"..4    ...    6..",			
		"...    .1.    ..2",			

		"5..    8..    .4.",			
		"...    7.9    8..",			
		"..2    ...    ..7",			
	};
	
	public static String[] s_times9656 = {
		
		"...    ..2    .9.",			
		".9.    ...    .5.",			
		"...    13.    ..4",			

		"..3    ...    .7.",			
		"..6    ..4    9.5",			
		"2..    .7.    8..",			

		"...    .18    ..7",			
		"65.    7..    ..9",			
		"..7    .4.    28.",			
	};
	
	public static String[] s_times9688 = {
		
		"...    ...    .6.",			
		"...    3..    28.",			
		"...    .15    ..9",			

		".5.    .7.    .1.",			
		"..1    4..    ..7",			
		"..4    ...    8.6",			

		".8.    ..2    .7.",			
		"34.    6..    9..",			
		"..5    .47    ...",			
	};
	
	// Template for copying to create a new initial grid
	public static String[] s_empty = {
		
		"...    ...    ...",			
		"...    ...    ...",			
		"...    ...    ...",			

		"...    ...    ...",			
		"...    ...    ...",			
		"...    ...    ...",			

		"...    ...    ...",			
		"...    ...    ...",			
		"...    ...    ...",			
	};
}
