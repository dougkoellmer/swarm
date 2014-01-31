package swarm.client.js;

public class U_Caching
{
	public static String calcRandomVersion()
	{
		return "?v=" + Math.random() * Integer.MAX_VALUE + "";
	}
}
