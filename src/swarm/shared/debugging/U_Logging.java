package swarm.shared.debugging;

import java.util.logging.Logger;

public final class U_Logging
{
	//private static final NumberFormat s_format = NumberFormat.get();
	
	private U_Logging()
	{
		
	}
	
	public static Logger getLogger(Class T)
	{
		return Logger.getLogger(T.getName());
	}
	
	public static String toFixed(double value)
	{
		return "" + value;
	}
}
