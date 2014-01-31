package swarm.server.session;

import java.security.SecureRandom;

import swarm.server.account.S_ServerAccount;
import swarm.server.account.U_Hashing;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonKeySource;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

class SessionCookieValue extends A_JsonEncodable implements I_BlobKey
{
	private static enum JsonKey implements I_JsonKeySource
	{
		TOKEN,
		ACCOUNT_ID,
		TOKEN_SALT;

		@Override
		public String getCompiledKey()
		{
			return this.ordinal() + "";
		}

		@Override
		public String getVerboseKey()
		{
			return this.name();
		}
		
	}
	
	private String m_token;
	private String m_tokenSalt;
	private Integer m_accountId;
	private String m_hashedToken;
	private final E_SessionType m_type;
	
	public SessionCookieValue(A_JsonFactory jsonFactory, I_JsonObject json, E_SessionType type)
	{
		super(jsonFactory, json);
		
		m_type = type;
	}
	
	public SessionCookieValue(Integer accountId, E_SessionType type)
	{
		m_token = U_Hashing.calcRandomSaltString(S_Session.SESSION_TOKEN_BYTES);
		m_tokenSalt = U_Hashing.calcRandomSaltString(S_Session.SESSION_TOKEN_BYTES);
		m_accountId = accountId;
		
		m_hashedToken = U_Hashing.hashWithSalt(m_token, m_tokenSalt);
		
		m_type = type;
	}
	
	public Integer getAccountId()
	{
		return m_accountId;
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, JsonKey.TOKEN, m_token);
		factory.getHelper().putInt(json_out, JsonKey.ACCOUNT_ID, m_accountId);
		factory.getHelper().putString(json_out, JsonKey.TOKEN_SALT, m_tokenSalt);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_token = factory.getHelper().getString(json, JsonKey.TOKEN);
		m_accountId = factory.getHelper().getInt(json, JsonKey.ACCOUNT_ID);
		m_tokenSalt = factory.getHelper().getString(json, JsonKey.TOKEN_SALT);
		
		m_hashedToken = U_Hashing.hashWithSalt(m_token, m_tokenSalt);
	}

	@Override
	public String createBlobKey(I_Blob blob)
	{
		return U_Blob.generateKey(blob, m_type.getBlobKeyComponent(), m_hashedToken);
	}
}
