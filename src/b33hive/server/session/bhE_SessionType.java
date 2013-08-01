package com.b33hive.server.session;

public enum bhE_SessionType
{
	TRANSIENT		("bh_sesh_trans",	"trans"),
	PERSISTENT		("bh_sesh_pers",	"pers");
	
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
