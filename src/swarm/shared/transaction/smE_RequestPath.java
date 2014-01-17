package swarm.shared.transaction;

/**
 * Generally I have enums formatted LIKE_THIS, but the camelCase formatting here makes it
 * easier to map these enums to their transaction handler counterparts.
 * 
 * @author Doug
 *
 */
public enum smE_RequestPath implements smI_RequestPath
{
	// !!!!IMPORTANT!!!!!  If you change the order of these after initial release, make sure to bump the server version also, in smS_App.
	
	getUserData				(smE_HttpMethod.GET),
	getGridData				(smE_HttpMethod.GET),
	getCode					(smE_HttpMethod.GET),
	syncCode				(smE_HttpMethod.POST),
	//previewCode				(smE_HttpMethod.POST),
	signIn					(smE_HttpMethod.POST),
	signUp					(smE_HttpMethod.POST),
	signOut					(smE_HttpMethod.POST),
	setNewDesiredPassword	(smE_HttpMethod.POST),
	getAccountInfo			(smE_HttpMethod.GET),
	getCellAddressMapping	(smE_HttpMethod.GET),
	getCellAddress			(smE_HttpMethod.GET),
	getStartingPosition		(smE_HttpMethod.GET),
	getServerVersion		(smE_HttpMethod.GET),
	getHashedPassword		(smE_HttpMethod.GET),
	getPasswordChangeToken	(smE_HttpMethod.GET);
	
	private final smE_HttpMethod m_method;
	
	private smE_RequestPath(smE_HttpMethod method)
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
		return smE_RequestPathBlock.LIB.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
