package swarm.server.transaction;

import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smE_RequestPathBlock;
import swarm.shared.transaction.smI_RequestPath;

public enum smE_DebugRequestPath implements smI_RequestPath
{
	sessionQueryTest;
	
	
	private final smE_HttpMethod m_method;
	
	private smE_DebugRequestPath()
	{
		m_method = smE_HttpMethod.POST;
	}
	
	@Override
	public smE_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return smE_RequestPathBlock.LIB_DEBUG.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
