package swarm.server.session;

import java.security.SecureRandom;

import swarm.server.account.smS_ServerAccount;
import swarm.server.account.smU_Hashing;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smU_Blob;
import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smI_JsonKeySource;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

class smSessionCookieValue extends smA_JsonEncodable implements smI_BlobKey
{
	private static enum JsonKey implements smI_JsonKeySource
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
	private final smE_SessionType m_type;
	
	public smSessionCookieValue(smI_JsonObject json, smE_SessionType type)
	{
		super(json);
		
		m_type = type;
	}
	
	public smSessionCookieValue(Integer accountId, smE_SessionType type)
	{
		m_token = smU_Hashing.calcRandomSaltString(smS_Session.SESSION_TOKEN_BYTES);
		m_tokenSalt = smU_Hashing.calcRandomSaltString(smS_Session.SESSION_TOKEN_BYTES);
		m_accountId = accountId;
		
		m_hashedToken = smU_Hashing.hashWithSalt(m_token, m_tokenSalt);
		
		m_type = type;
	}
	
	public Integer getAccountId()
	{
		return m_accountId;
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, JsonKey.TOKEN, m_token);
		sm.jsonFactory.getHelper().putInt(json, JsonKey.ACCOUNT_ID, m_accountId);
		sm.jsonFactory.getHelper().putString(json, JsonKey.TOKEN_SALT, m_tokenSalt);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		m_token = sm.jsonFactory.getHelper().getString(json, JsonKey.TOKEN);
		m_accountId = sm.jsonFactory.getHelper().getInt(json, JsonKey.ACCOUNT_ID);
		m_tokenSalt = sm.jsonFactory.getHelper().getString(json, JsonKey.TOKEN_SALT);
		
		m_hashedToken = smU_Hashing.hashWithSalt(m_token, m_tokenSalt);
	}

	@Override
	public String createBlobKey(smI_Blob blob)
	{
		return smU_Blob.generateKey(blob, m_type.getBlobKeyComponent(), m_hashedToken);
	}
}
