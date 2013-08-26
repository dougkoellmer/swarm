package swarm.shared.json;

import swarm.shared.app.sm;



/**
 * Partial convenience implementation of smI_JsonEncodable.
 * Objects that subclass this get passed back and forth between client and server or client and some local persistence layer.
 * Objects sometimes also further subclass themselves into client and server versions, with
 * the shared base version reading/writing common data.
 * 
 * @author Doug
 *
 */
public abstract class smA_JsonEncodable implements smI_JsonEncodable
{
	public abstract void writeJson(smI_JsonObject json);
	public abstract void readJson(smI_JsonObject json);
	
	public smA_JsonEncodable()
	{
	}
	
	public smA_JsonEncodable(smI_JsonObject json)
	{
		this.readJson(json);
	}
	
	@Override
	public void readJson(String json)
	{
		smA_JsonFactory jsonFactory = sm.jsonFactory;
		smI_JsonObject jsonObject = jsonFactory.createJsonObject(json);
		this.readJson(jsonObject);
	}
	
	@Override
	public smI_JsonObject writeJson()
	{
		smA_JsonFactory jsonFactory = sm.jsonFactory;
		smI_JsonObject json = jsonFactory.createJsonObject();
		this.writeJson(json);
		return json;
	}	
	
	@Override
	public boolean isEqualTo(smI_JsonObject json)
	{
		return false;
	}
	
	@Override
	public boolean isEqualTo(smI_JsonEncodable jsonEncodable)
	{
		return this.isEqualTo(jsonEncodable.writeJson());
	}
}
