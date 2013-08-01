package com.b33hive.shared.json;

public interface bhI_JsonEncodable
{
	void readJson(bhI_JsonObject json);
	void readJson(String json);
	
	void writeJson(bhI_JsonObject json);
	bhI_JsonObject writeJson();
	
	boolean isEqualTo(bhI_JsonObject json);
	boolean isEqualTo(bhI_JsonEncodable jsonEncodable);
}
