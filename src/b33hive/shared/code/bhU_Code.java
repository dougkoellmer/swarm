package com.b33hive.shared.code;

import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhE_CellAddressParseError;
import com.b33hive.shared.structs.bhE_NetworkPrivilege;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public final class bhU_Code
{
	public static String transformPathToJavascript(String b33hivePath)
	{
		//TODO: Should ecmascript the string, except for forward slashes.
		//StringEscapeUtils.escapeEcmaScript();
		
		return "javascript:bh.snap('"+b33hivePath+"');";
	}
}
