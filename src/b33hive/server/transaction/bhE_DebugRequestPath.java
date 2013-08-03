package b33hive.server.transaction;

import b33hive.shared.transaction.bhE_HttpMethod;
import b33hive.shared.transaction.bhI_RequestPath;

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
		return this.ordinal() + 1000000*2;
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
