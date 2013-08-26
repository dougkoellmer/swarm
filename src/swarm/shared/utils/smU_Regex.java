package swarm.shared.utils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class smU_Regex
{
	public static boolean calcIsMatch(String text, String regex)
	{
		RegExp regExp = RegExp.compile(regex);
		MatchResult matcher = regExp.exec(text);
		boolean matchFound = (matcher != null); // equivalent to regExp.test(inputStr); 
		return matchFound;
	}
	
	private smU_Regex()
	{
		
	}
}
