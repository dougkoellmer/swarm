package swarm.server.debugging;

import swarm.server.data.sql.bhI_SqlEncodable;
import swarm.shared.debugging.bhTelemetryAssert;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.structs.bhTuple;

public class bhServerTelemetryAssert extends bhTelemetryAssert implements bhI_SqlEncodable
{
	private final String m_ip;
	private final Integer m_accountId;
	
	private int m_progress = -1;
	
	public bhServerTelemetryAssert(bhI_JsonObject json, String ip, Integer accountId)
	{
		super(json);

		m_ip = ip;
		m_accountId = accountId;
	}

	@Override
	public String getTable()
	{
		return "assert";
	}

	@Override
	public bhTuple<String, Object> nextColumn()
	{
		m_progress++;
		
		switch(m_progress)
		{
			case 0:		return new bhTuple<String, Object>("account_id",	m_accountId);
			case 1:		return new bhTuple<String, Object>("message",		m_message);
			case 2:		return new bhTuple<String, Object>("platform",		m_platform);
			case 3:		return new bhTuple<String, Object>("ip",			m_ip);
			
			default:	m_progress = -1;
		}
		
		return null;
	}
}
