package b33hive.shared.account;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;

public class bhResetPasswordResult extends bhA_JsonEncodable
{
	public bhResetPasswordResult()
	{
		init();
	}
	
	public bhResetPasswordResult(bhI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public void setResponseError()
	{
	}
	
	private void init()
	{
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
	
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
	}
}
