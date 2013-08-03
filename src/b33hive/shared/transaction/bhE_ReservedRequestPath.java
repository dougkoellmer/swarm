package b33hive.shared.transaction;

public enum bhE_ReservedRequestPath implements bhI_RequestPath
{
	batch				(bhE_HttpMethod.GET);
	
	private final bhE_HttpMethod m_method;
	
	private bhE_ReservedRequestPath(bhE_HttpMethod method)
	{
		m_method = method;
	}
	
	@Override
	public bhE_HttpMethod getDefaultMethod()
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
