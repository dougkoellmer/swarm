package swarm.shared.transaction;

public enum E_ReservedRequestPath implements I_RequestPath
{
	batch				(E_HttpMethod.GET);
	
	private final E_HttpMethod m_method;
	
	private E_ReservedRequestPath(E_HttpMethod method)
	{
		m_method = method;
	}
	
	@Override
	public E_HttpMethod getDefaultMethod()
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
