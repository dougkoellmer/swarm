package swarm.server.app;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.JsonHelper;

public abstract class A_ServerJsonFactory extends A_JsonFactory
{
	public abstract void startScope(boolean verboseKeys);
	
	public abstract void endScope();
}
