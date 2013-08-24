package swarm.client.time;

import java.util.Date;

public class bhU_Time
{
	private static long s_startTime = 0;
	
	private bhU_Time()
	{
		
	}
	
	public static void startUp()
	{
		s_startTime = (new Date()).getTime();
	}
	
	public static long getMilliseconds()
	{
		return (new Date()).getTime() - s_startTime;
	}
	
	public static double getSeconds()
	{
		return ((double)getMilliseconds())/1000.0;
	}
}
