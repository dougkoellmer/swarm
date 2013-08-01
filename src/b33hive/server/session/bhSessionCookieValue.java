package com.b33hive.server.session;

import java.security.SecureRandom;

import com.b33hive.server.account.bhS_ServerAccount;
import com.b33hive.server.account.bhU_Hashing;
import com.b33hive.server.data.blob.bhI_Blob;
import com.b33hive.server.data.blob.bhI_BlobKeySource;
import com.b33hive.server.data.blob.bhU_Blob;
import com.b33hive.shared.json.bhA_JsonEncodable;
import com.b33hive.shared.json.bhI_JsonKeySource;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;

class bhSessionCookieValue extends bhA_JsonEncodable implements bhI_BlobKeySource
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
		bhJsonHelper.getInstance().putString(json, JsonKey.TOKEN, m_token);
		bhJsonHelper.getInstance().putInt(json, JsonKey.ACCOUNT_ID, m_accountId);
		bhJsonHelper.getInstance().putString(json, JsonKey.TOKEN_SALT, m_tokenSalt);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_token = bhJsonHelper.getInstance().getString(json, JsonKey.TOKEN);
		m_accountId = bhJsonHelper.getInstance().getInt(json, JsonKey.ACCOUNT_ID);
		m_tokenSalt = bhJsonHelper.getInstance().getString(json, JsonKey.TOKEN_SALT);
		
		m_hashedToken = bhU_Hashing.hashWithSalt(m_token, m_tokenSalt);
	}

	@Override
	public String createBlobKey(bhI_Blob blob)
	{
		return bhU_Blob.generateKey(blob, m_type.getBlobKeyComponent(), m_hashedToken);
	}
}
