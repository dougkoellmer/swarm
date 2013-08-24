package swarm.server.session;

import java.security.SecureRandom;

import swarm.server.account.bhS_ServerAccount;
import swarm.server.account.bhU_Hashing;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhU_Blob;
import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhI_JsonKeySource;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

class bhSessionCookieValue extends bhA_JsonEncodable implements bhI_BlobKey
{
	private static enum JsonKey implements bhI_JsonKeySource
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
	private final bhE_SessionType m_type;
	
	public bhSessionCookieValue(bhI_JsonObject json, bhE_SessionType type)
	{
		super(json);
		
		m_type = type;
	}
	
	public bhSessionCookieValue(Integer accountId, bhE_SessionType type)
	{
		m_token = bhU_Hashing.calcRandomSaltString(bhS_Session.SESSION_TOKEN_BYTES);
		m_tokenSalt = bhU_Hashing.calcRandomSaltString(bhS_Session.SESSION_TOKEN_BYTES);
		m_accountId = accountId;
		
		m_hashedToken = bhU_Hashing.hashWithSalt(m_token, m_tokenSalt);
		
		m_type = type;
	}
	
	public Integer getAccountId()
	{
		return m_accountId;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, JsonKey.TOKEN, m_token);
		sm.jsonFactory.getHelper().putInt(json, JsonKey.ACCOUNT_ID, m_accountId);
		sm.jsonFactory.getHelper().putString(json, JsonKey.TOKEN_SALT, m_tokenSalt);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_token = sm.jsonFactory.getHelper().getString(json, JsonKey.TOKEN);
		m_accountId = sm.jsonFactory.getHelper().getInt(json, JsonKey.ACCOUNT_ID);
		m_tokenSalt = sm.jsonFactory.getHelper().getString(json, JsonKey.TOKEN_SALT);
		
		m_hashedToken = bhU_Hashing.hashWithSalt(m_token, m_tokenSalt);
	}

	@Override
	public String createBlobKey(bhI_Blob blob)
	{
		return bhU_Blob.generateKey(blob, m_type.getBlobKeyComponent(), m_hashedToken);
	}
}
