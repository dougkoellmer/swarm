package com.b33hive.shared.account;

import com.b33hive.shared.json.bhA_JsonEncodable;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;

public abstract class bhA_AccountCredentials extends bhA_JsonEncodable
{
	protected String[] m_credentials;
	private boolean m_rememberMe;
	
	public bhA_AccountCredentials(bhI_JsonObject json)
	{
		super(json);
	}
	
	public bhA_AccountCredentials(boolean rememberMe)
	{
		m_rememberMe = rememberMe;
	}
	
	public boolean rememberMe()
	{
		return m_rememberMe;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_rememberMe = bhJsonHelper.getInstance().getBoolean(json, bhE_JsonKey.rememberMe);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhJsonHelper.getInstance().putBoolean(json, bhE_JsonKey.rememberMe, m_rememberMe);
	}
}
