package swarm.server.transaction;

import swarm.server.app.ServerContext;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.session.SessionManager;
import swarm.shared.json.A_JsonFactory;

public abstract class A_DefaultRequestHandler implements I_RequestHandler
{
	protected ServerContext m_serverContext;
	
	public void init(ServerContext context)
	{
		m_serverContext = context;
	}
}
