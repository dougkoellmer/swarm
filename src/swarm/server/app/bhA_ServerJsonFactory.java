package swarm.server.app;

import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhJsonHelper;

public abstract class bhA_ServerJsonFactory extends bhA_JsonFactory
{
	public abstract void startScope(boolean verboseKeys);
	
	public abstract void endScope();
}
