package b33hive.shared.code;

import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhE_NetworkPrivilege;
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
