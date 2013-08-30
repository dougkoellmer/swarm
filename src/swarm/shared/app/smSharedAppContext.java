package swarm.shared.app;

import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.transaction.smRequestPathManager;

public class smSharedAppContext 
{
	public smRequestPathManager requestPathMngr;
	public smA_JsonFactory jsonFactory;
	public smA_CodeCompiler codeCompiler;
}
