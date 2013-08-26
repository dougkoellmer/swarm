package swarm.shared.transaction;

/**
 * @author Doug
 *
 */
public enum smE_TelemetryRequestPath implements smI_RequestPath
{
	logAssert;
	
	private final smE_HttpMethod m_method;
	
	private smE_TelemetryRequestPath()
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
		return smE_RequestPathBlock.LIB_TELEMETRY.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
