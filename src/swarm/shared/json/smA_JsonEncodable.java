package swarm.shared.json;

import swarm.shared.app.smSharedAppContext;



/**
 * TODO: Get rid of this class...used to play a role, but pretty useless now.
 * 
 * @author Doug
 *
 */
public abstract class smA_JsonEncodable implements smI_ReadsJson, smI_WritesJson
{
	public abstract void writeJson(smA_JsonFactory factory, smI_JsonObject json_out);
	public abstract void readJson(smA_JsonFactory factory, smI_JsonObject json);
	
	public smA_JsonEncodable()
	{
	}
	
	public smA_JsonEncodable(smA_JsonFactory factory, smI_JsonObject json)
	{
		this.readJson(factory, json);
	}
}
