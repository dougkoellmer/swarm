package swarm.shared.account;

import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;

public class smResetPasswordResult extends smA_JsonEncodable
{
	public smResetPasswordResult()
	{
		init();
	}
	
	public smResetPasswordResult(smI_JsonObject json)
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
	public void writeJson(smI_JsonObject json)
	{
	
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		init();
	}
}
