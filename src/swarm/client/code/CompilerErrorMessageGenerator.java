package swarm.client.code;

import java.util.List;

import swarm.shared.app.S_CommonApp;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.CompilerMessage;
import swarm.shared.debugging.U_Debug;

public class CompilerErrorMessageGenerator
{
	private CompilerErrorMessageGenerator()
	{
		
	}
	
	public String generate(CompilerResult result)
	{
		List<CompilerMessage> messages = result.getMessages();
		
		if( messages != null && messages.size() > 0 )
		{
			String html = "<div style='width:95%; text-align:left;'><ul>";
			
			for( int i = 0; i < messages.size(); i++ )
			{
				CompilerMessage error = messages.get(i);
				html += "<li>";
				html += error.getMessage();
				html += "</li>";
			}
			html += "</ul></div>";
			
			return html;
		}
		else
		{
			switch( result.getStatus() )
			{
				case NO_ERROR:
				{
					U_Debug.ASSERT(false, "NO_ERROR returned but tried to generate compiler error message for it.");
					
					return null;
				}
				
				case TOO_LONG:
				{
					return "Your source is too long. Please shorten it and try again.";
				}
				
				case RESPONSE_ERROR:
				{
					return "The server had a small hiccup while processing your save. Please try again later.";
				}
				
				case COMPILER_EXCEPTION:
				{
					return "An unknown problem occurred with the compiler...please try again later.";
				}
				
				case COMPILATION_ERRORS:
				{
					U_Debug.ASSERT(false, "COMPILATION_ERRORS returned but with no messages.");
					
					return "Unknown compiler errors occured.";
				}
			}
		}
		
		return null;
	}
}
