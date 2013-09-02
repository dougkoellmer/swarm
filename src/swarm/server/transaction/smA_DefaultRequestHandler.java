package swarm.server.transaction;

import swarm.server.app.smServerContext;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.session.smSessionManager;
import swarm.shared.json.smA_JsonFactory;

public abstract class smA_DefaultRequestHandler implements smI_RequestHandler
{
	protected smServerContext m_context;
	
	public void init(smServerContext context)
	{
		m_context = context;
	}
}
