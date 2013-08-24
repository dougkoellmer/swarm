package swarm.shared.account;

import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;

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
