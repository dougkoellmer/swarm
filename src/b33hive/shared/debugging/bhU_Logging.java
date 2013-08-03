package b33hive.shared.debugging;

import java.util.logging.Logger;

public final class bhU_Logging
{
	//private static final NumberFormat s_format = NumberFormat.get();
	
	private bhU_Logging()
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
