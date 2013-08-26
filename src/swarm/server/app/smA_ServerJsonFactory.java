package swarm.server.app;

import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smJsonHelper;

public abstract class smA_ServerJsonFactory extends smA_JsonFactory
{
	public abstract void startScope(boolean verboseKeys);
	
	public abstract void endScope();
}
