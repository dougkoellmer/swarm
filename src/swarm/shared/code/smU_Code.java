package swarm.shared.code;

import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smE_NetworkPrivilege;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public final class smU_Code
{
	public static String transformPathToJavascript(String cellAddress)
	{
		//TODO: Should ecmascript the string, except for forward slashes.
		//StringEscapeUtils.escapeEcmaScript();
		
		return "javascript:sm.snap('"+cellAddress+"');"; // TODO: Get API namespace from somewhere.
	}
}
