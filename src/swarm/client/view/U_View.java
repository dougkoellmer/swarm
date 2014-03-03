package swarm.client.view;

public class U_View
{
	public static double easeMantissa(double mantissa)
	{
		if( mantissa == 0 )
		{
			return 0;
		}
		else
		{
			return Math.pow(mantissa, 1.0/5.0);
		}
	}
}
