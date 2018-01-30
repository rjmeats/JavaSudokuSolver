
public class FormatUtils {

	static String padRight(String s, int width, char padChar)
	{	
		StringBuilder sb = new StringBuilder();
		sb.append((s == null) ? "" : s);
		
		while(sb.length() < width)
		{
			sb.append(padChar);
		}
		
		return sb.toString();
	}


	static String padRight(String s, int width)
	{
		return padRight(s, width, ' ');
	}

	static String padRight(int n, int width)
	{
		String s = Integer.toString(n);
		return padRight(s, width, ' ');
	}
}
