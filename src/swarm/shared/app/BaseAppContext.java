package swarm.shared.app;

import swarm.shared.code.A_CodeCompiler;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.transaction.RequestPathManager;

public class BaseAppContext 
{
	public RequestPathManager requestPathMngr;
	public A_JsonFactory jsonFactory;
	public A_CodeCompiler codeCompiler;
}
