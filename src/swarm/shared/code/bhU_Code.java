package swarm.shared.code;

import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhE_NetworkPrivilege;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public final class bhU_Code
{
	public static String transformPathToJavascript(String cellAddress)
	{
		//TODO: Should ecmascript the string, except for forward slashes.
		//StringEscapeUtils.escapeEcmaScript();
		
		return "javascript:bh.snap('"+cellAddress+"');"; // TODO: Get API namespace from somewhere.
	}
}
