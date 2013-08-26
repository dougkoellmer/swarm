package swarm.shared.json;

public interface smI_JsonEncodable
{
	void readJson(smI_JsonObject json);
	void readJson(String json);
	
	void writeJson(smI_JsonObject json);
	smI_JsonObject writeJson();
	
	boolean isEqualTo(smI_JsonObject json);
	boolean isEqualTo(smI_JsonEncodable jsonEncodable);
}
