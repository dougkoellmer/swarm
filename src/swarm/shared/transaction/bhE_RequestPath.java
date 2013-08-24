package swarm.shared.transaction;

/**
 * Generally I have enums formatted LIKE_THIS, but the camelCase formatting here makes it
 * easier to map these enums to their transaction handler counterparts.
 * 
 * @author Doug
 *
 */
public enum bhE_RequestPath implements bhI_RequestPath
{
	// !!!!IMPORTANT!!!!!  If you change the order of these after initial release, make sure to bump the server version also, in bhS_App.
	
	getUserData				(bhE_HttpMethod.GET),
	getGridData				(bhE_HttpMethod.GET),
	getCode					(bhE_HttpMethod.GET),
	syncCode				(bhE_HttpMethod.POST),
	//previewCode				(bhE_HttpMethod.POST),
	signIn					(bhE_HttpMethod.POST),
	signUp					(bhE_HttpMethod.POST),
	signOut					(bhE_HttpMethod.POST),
	setNewDesiredPassword	(bhE_HttpMethod.POST),
	getAccountInfo			(bhE_HttpMethod.GET),
	getCellAddressMapping	(bhE_HttpMethod.GET),
	getCellAddress			(bhE_HttpMethod.GET),
	getStartingPosition		(bhE_HttpMethod.GET),
	getServerVersion		(bhE_HttpMethod.GET),
	getPasswordChangeToken	(bhE_HttpMethod.GET);
	
	private final bhE_HttpMethod m_method;
	
	private bhE_RequestPath(bhE_HttpMethod method)
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
		return bhE_RequestPathBlock.LIB.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
