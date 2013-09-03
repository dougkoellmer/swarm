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
	
	public smResetPasswordResult(smA_JsonFactory jsonFactory, smI_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	public void setResponseError()
	{
	}
	
	private void init()
	{
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
	
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		init();
	}
}
