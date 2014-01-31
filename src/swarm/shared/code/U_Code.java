package swarm.shared.code;

import swarm.shared.structs.CellAddress;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_NetworkPrivilege;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public final class U_Code
{
	public static String transformPathToJavascript(String apiNamespace, String cellAddress)
	{
		//TODO: Should ecmascript the string, except for forward slashes.
		//StringEscapeUtils.escapeEcmaScript();
		
		return "javascript:"+apiNamespace+".snap('"+cellAddress+"');"; // TODO: Get API namespace from somewhere.
	}
}
