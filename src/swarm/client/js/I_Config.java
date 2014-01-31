package swarm.client.js;

public interface I_Config
{
	int getInt(String property);
	
	float getFloat(String property);
	
	double getDouble(String property);
	
	boolean getBool(String property);
}
