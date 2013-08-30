package swarm.server.transaction;

import swarm.shared.json.smA_JsonFactory;

public abstract class smA_DefaultRequestHandler implements smI_RequestHandler
{
	protected smA_JsonFactory m_jsonFactory;
	
	public void init(smA_JsonFactory jsonFactory)
	{
		m_jsonFactory = jsonFactory;
	}
}
