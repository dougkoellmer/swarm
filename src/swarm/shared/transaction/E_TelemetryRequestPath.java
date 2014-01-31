package swarm.shared.transaction;

/**
 * @author Doug
 *
 */
public enum E_TelemetryRequestPath implements I_RequestPath
{
	logAssert;
	
	private final E_HttpMethod m_method;
	
	private E_TelemetryRequestPath()
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
		return E_RequestPathBlock.LIB_TELEMETRY.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
