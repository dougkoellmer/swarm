package swarm.server.debugging;

import swarm.server.data.sql.smI_SqlEncodable;
import swarm.shared.debugging.smTelemetryAssert;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smTuple;

public class smServerTelemetryAssert extends smTelemetryAssert implements smI_SqlEncodable
{
	private final String m_ip;
	private final Integer m_accountId;
	
	private int m_progress = -1;
	
	public smServerTelemetryAssert(smI_JsonObject json, String ip, Integer accountId)
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
	public smTuple<String, Object> nextColumn()
	{
		m_progress++;
		
		switch(m_progress)
		{
			case 0:		return new smTuple<String, Object>("account_id",	m_accountId);
			case 1:		return new smTuple<String, Object>("message",		m_message);
			case 2:		return new smTuple<String, Object>("platform",		m_platform);
			case 3:		return new smTuple<String, Object>("ip",			m_ip);
			
			default:	m_progress = -1;
		}
		
		return null;
	}
}
