package b33hive.shared.transaction;

/**
 * @author Doug
 *
 */
public enum bhE_TelemetryRequestPath implements bhI_RequestPath
{
	logAssert;
	
	private final bhE_HttpMethod m_method;
	
	private bhE_TelemetryRequestPath()
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
		return this.ordinal() + 1000000*3;
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
