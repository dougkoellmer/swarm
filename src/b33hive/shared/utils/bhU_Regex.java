package com.b33hive.shared;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class bhU_Regex
{
	public static boolean calcIsMatch(String text, String regex)
	{
		RegExp regExp = RegExp.compile(regex);
		MatchResult matcher = regExp.exec(text);
		boolean matchFound = (matcher != null); // equivalent to regExp.test(inputStr); 
		return matchFound;
	}
	
	private bhU_Regex()
	{
		
	}
}
