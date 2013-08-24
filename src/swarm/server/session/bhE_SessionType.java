package swarm.server.session;

public enum bhE_SessionType
{
	TRANSIENT		("sm_sesh_trans",	"trans"),
	PERSISTENT		("sm_sesh_pers",	"pers");
	
	private final String m_cookieName;
	private final String m_blobKeyComponent;
	
	private bhE_SessionType(String cookieName, String blobKeyComponent)
	{
		m_cookieName = cookieName;
		m_blobKeyComponent = blobKeyComponent;
	}
	
	public String getCookieName()
	{
		return m_cookieName;
	}
	
	public String getBlobKeyComponent()
	{
		return m_blobKeyComponent;
	}
}
