package swarm.client.structs;

import swarm.shared.account.bhE_SignUpCredentialType;
import swarm.shared.account.bhSignUpCredentials;
import swarm.shared.account.bhU_Account;
import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public class bhAccountInfo extends bhA_JsonEncodable
{
	public static enum Type
	{
		USERNAME
	};
	
	private String[] m_type = new String[Type.values().length];
	
	public bhAccountInfo(String ... types)
	{
		for(int i = 0; i < types.length; i++ )
		{
			m_type[i] = types[i];
		}
		
		this.toLowerCase();
	}
	
	public String get(Type type)
	{
		return m_type[type.ordinal()];
	}
	
	private void toLowerCase()
	{
		for(int i = 0; i < m_type.length; i++ )
		{
			if( m_type[i] != null )
			{
				m_type[i] = m_type[i].toLowerCase();
			}
		}
	}
	
	public void copyCredentials(bhSignUpCredentials credentials)
	{
		m_type[Type.USERNAME.ordinal()] = credentials.get(bhE_SignUpCredentialType.USERNAME);
		
		this.toLowerCase();
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_type.length; i++ )
		{
			creds.addString(m_type[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.accountInfo, creds);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		bhI_JsonArray info = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.accountInfo);
		
		for( int i = 0; i < info.getSize(); i++ )
		{
			m_type[i] = info.getString(i);
		}
		
		this.toLowerCase();
	}
}
