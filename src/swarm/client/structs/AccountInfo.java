package swarm.client.structs;

import swarm.shared.account.E_SignUpCredentialType;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.U_Account;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class AccountInfo extends A_JsonEncodable
{
	public static enum Type
	{
		USERNAME
	};
	
	private String[] m_type = new String[Type.values().length];
	
	public AccountInfo(String ... types)
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
	
	public void copyCredentials(SignUpCredentials credentials)
	{
		m_type[Type.USERNAME.ordinal()] = credentials.get(E_SignUpCredentialType.USERNAME);
		
		this.toLowerCase();
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		I_JsonArray creds = factory.createJsonArray();
		
		for( int i = 0; i < m_type.length; i++ )
		{
			creds.addString(m_type[i]);
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.accountInfo, creds);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		I_JsonArray info = factory.getHelper().getJsonArray(json, E_JsonKey.accountInfo);
		
		for( int i = 0; i < info.getSize(); i++ )
		{
			m_type[i] = info.getString(i);
		}
		
		this.toLowerCase();
	}
}
