package swarm.client.structs;

import swarm.shared.account.smE_SignUpCredentialType;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smU_Account;
import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smAccountInfo extends smA_JsonEncodable
{
	public static enum Type
	{
		USERNAME
	};
	
	private String[] m_type = new String[Type.values().length];
	
	public smAccountInfo(String ... types)
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
	
	public void copyCredentials(smSignUpCredentials credentials)
	{
		m_type[Type.USERNAME.ordinal()] = credentials.get(smE_SignUpCredentialType.USERNAME);
		
		this.toLowerCase();
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		smI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_type.length; i++ )
		{
			creds.addString(m_type[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.accountInfo, creds);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		smI_JsonArray info = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.accountInfo);
		
		for( int i = 0; i < info.getSize(); i++ )
		{
			m_type[i] = info.getString(i);
		}
		
		this.toLowerCase();
	}
}
