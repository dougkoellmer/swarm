package b33hive.server.thirdparty.servlet;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class bhU_Jsp
{
	public static void writeJavascriptVariable(JspWriter out, String variableName, String value) throws IOException
	{
		value = value == null ? "null" : "'"+value+"'";
		
		out.println("var "+variableName+" = "+value+";");
	}
}
