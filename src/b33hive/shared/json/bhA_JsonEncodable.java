package com.b33hive.shared.json;

/**
 * Partial convenience implementation of bhI_JsonEncodable.
 * Objects that subclass this get passed back and forth between client and server or client and some local persistence layer.
 * Objects sometimes also further subclass themselves into client and server versions, with
 * the shared base version reading/writing common data.
 * 
 * @author Doug
 *
 */
public abstract class bhA_JsonEncodable implements bhI_JsonEncodable
{
	public abstract void writeJson(bhI_JsonObject json);
	public abstract void readJson(bhI_JsonObject json);
	
	public bhA_JsonEncodable()
	{
	}
	
	public bhA_JsonEncodable(bhI_JsonObject json)
	{
		this.readJson(json);
	}
	
	@Override
	public void readJson(String json)
	{
		bhI_JsonObject jsonObject = bhA_JsonFactory.getInstance().createJsonObject(json);
		this.readJson(jsonObject);
	}
	
	@Override
	public bhI_JsonObject writeJson()
	{
		bhI_JsonObject json = bhA_JsonFactory.getInstance().createJsonObject();
		this.writeJson(json);
		return json;
	}	
	
	@Override
	public boolean isEqualTo(bhI_JsonObject json)
	{
		return false;
	}
	
	@Override
	public boolean isEqualTo(bhI_JsonEncodable jsonEncodable)
	{
		return this.isEqualTo(jsonEncodable.writeJson());
	}
}
