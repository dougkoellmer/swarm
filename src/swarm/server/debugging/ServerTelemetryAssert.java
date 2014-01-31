package swarm.server.debugging;

import swarm.server.data.sql.I_SqlEncodable;
import swarm.shared.debugging.TelemetryAssert;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.Tuple;

public class ServerTelemetryAssert extends TelemetryAssert implements I_SqlEncodable
{
	private final String m_ip;
	private final Integer m_accountId;
	
	private int m_progress = -1;
	
	public ServerTelemetryAssert(A_JsonFactory jsonFactory, I_JsonObject json, String ip, Integer accountId)
	{
		super(jsonFactory, json);

		m_ip = ip;
		m_accountId = accountId;
	}

	@Override
	public String getTable()
	{
		return "assert";
	}

	@Override
	public Tuple<String, Object> nextColumn()
	{
		m_progress++;
		
		switch(m_progress)
		{
			case 0:		return new Tuple<String, Object>("account_id",	m_accountId);
			case 1:		return new Tuple<String, Object>("message",		m_message);
			case 2:		return new Tuple<String, Object>("platform",		m_platform);
			case 3:		return new Tuple<String, Object>("ip",			m_ip);
			
			default:	m_progress = -1;
		}
		
		return null;
	}
}
