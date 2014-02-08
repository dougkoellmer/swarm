package swarm.shared.transaction;

/**
 * Generally I have enums formatted LIKE_THIS, but the camelCase formatting here makes it
 * easier to map these enums to their transaction handler counterparts.
 * 
 * @author Doug
 *
 */
public enum E_RequestPath implements I_RequestPath
{
	// !!!!IMPORTANT!!!!!  If you change the order of these after initial release, make sure to bump the server version also, in smS_App.
	
	getUserData				(E_HttpMethod.GET),
	getGridData				(E_HttpMethod.GET),
	getCode					(E_HttpMethod.GET),
	syncCode				(E_HttpMethod.POST),
	//previewCode				(smE_HttpMethod.POST),
	signIn					(E_HttpMethod.POST),
	signUp					(E_HttpMethod.POST),
	signOut					(E_HttpMethod.POST),
	setNewDesiredPassword	(E_HttpMethod.POST),
	getAccountInfo			(E_HttpMethod.GET),
	getCellAddressMapping	(E_HttpMethod.GET),
	getCellAddress			(E_HttpMethod.GET),
	getStartingPosition		(E_HttpMethod.GET),
	getServerVersion		(E_HttpMethod.GET),
	getHashedPassword		(E_HttpMethod.GET),
	getPasswordChangeToken	(E_HttpMethod.GET),
	getFocusedCellSize		(E_HttpMethod.GET);
	
	private final E_HttpMethod m_method;
	
	private E_RequestPath(E_HttpMethod method)
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
		return E_RequestPathBlock.LIB.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
