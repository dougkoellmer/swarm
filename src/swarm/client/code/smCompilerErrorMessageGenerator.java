package swarm.client.code;

import java.util.List;

import swarm.shared.app.smS_App;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smCompilerMessage;
import swarm.shared.debugging.smU_Debug;

public class smCompilerErrorMessageGenerator
{
	private static final smCompilerErrorMessageGenerator s_instance = new smCompilerErrorMessageGenerator();
	
	private smCompilerErrorMessageGenerator()
	{
		
	}
	
	public static smCompilerErrorMessageGenerator getInstance()
	{
		return s_instance;
	}
	
	public String generate(smCompilerResult result)
	{
		List<smCompilerMessage> messages = result.getMessages();
		
		if( messages != null && messages.size() > 0 )
		{
			String html = "<div style='width:95%; text-align:left;'><ul>";
			
			for( int i = 0; i < messages.size(); i++ )
			{
				smCompilerMessage error = messages.get(i);
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
					smU_Debug.ASSERT(false, "NO_ERROR returned but tried to generate compiler error message for it.");
					
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
					smU_Debug.ASSERT(false, "COMPILATION_ERRORS returned but with no messages.");
					
					return "Unknown compiler errors occured.";
				}
			}
		}
		
		return null;
	}
}
