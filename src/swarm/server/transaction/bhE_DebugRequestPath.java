package swarm.server.transaction;

import swarm.shared.transaction.bhE_HttpMethod;
import swarm.shared.transaction.bhE_RequestPathBlock;
import swarm.shared.transaction.bhI_RequestPath;

public enum bhE_DebugRequestPath implements bhI_RequestPath
{
	sessionQueryTest;
	
	
	private final bhE_HttpMethod m_method;
	
	private bhE_DebugRequestPath()
	{
		m_method = bhE_HttpMethod.POST;
	}
	
	@Override
	public bhE_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return bhE_RequestPathBlock.LIB_DEBUG.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
