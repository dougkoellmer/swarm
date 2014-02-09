package swarm.shared.json;

import swarm.shared.app.BaseAppContext;



/**
 * TODO: Get rid of this class...used to play a role, but pretty useless now.
 * 
 * @author Doug
 *
 */
public abstract class A_JsonEncodable implements I_ReadsJson, I_WritesJson
{
	public abstract void writeJson(I_JsonObject json_out, A_JsonFactory factory);
	public abstract void readJson(I_JsonObject json, A_JsonFactory factory);
	
	public A_JsonEncodable()
	{
	}
	
	public A_JsonEncodable(A_JsonFactory factory, I_JsonObject json)
	{
		this.readJson(json, factory);
	}
}
