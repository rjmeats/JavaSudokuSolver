package puzzle;

public class FormatUtils {

	public static String padRight(String s, int width, char padChar)
	{	
		StringBuilder sb = new StringBuilder();
		sb.append((s == null) ? "" : s);
		
		while(sb.length() < width)
		{
			sb.append(padChar);
		}
		
		return sb.toString();
	}


	public static String padRight(String s, int width)
	{
		return padRight(s, width, ' ');
	}

	public static String padRight(int n, int width)
	{
		String s = Integer.toString(n);
		return padRight(s, width, ' ');
	}
}
