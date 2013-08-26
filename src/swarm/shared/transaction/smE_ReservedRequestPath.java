package swarm.shared.transaction;

public enum smE_ReservedRequestPath implements smI_RequestPath
{
	batch				(smE_HttpMethod.GET);
	
	private final smE_HttpMethod m_method;
	
	private smE_ReservedRequestPath(smE_HttpMethod method)
	{
		m_method = method;
	}
	
	@Override
	public smE_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return -(this.ordinal()+1);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
