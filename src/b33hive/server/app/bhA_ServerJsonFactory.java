package b33hive.server.app;

import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhJsonHelper;

public abstract class bhA_ServerJsonFactory extends bhA_JsonFactory
{
	public abstract void startScope();
	
	public abstract void endScope();
}
