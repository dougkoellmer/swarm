package swarm.shared.account;

import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;

public class ResetPasswordResult extends A_JsonEncodable
{
	public ResetPasswordResult()
	{
		init();
	}
	
	public ResetPasswordResult(A_JsonFactory jsonFactory, I_JsonObject json)
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
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
	
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		init();
	}
}
