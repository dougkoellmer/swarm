package swarm.server.transaction;

import swarm.shared.transaction.E_HttpMethod;
import swarm.shared.transaction.E_RequestPathBlock;
import swarm.shared.transaction.I_RequestPath;

public enum E_DebugRequestPath implements I_RequestPath
{
	sessionQueryTest;
	
	
	private final E_HttpMethod m_method;
	
	private E_DebugRequestPath()
	{
		m_method = E_HttpMethod.POST;
	}
	
	@Override
	public E_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return E_RequestPathBlock.LIB_DEBUG.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
