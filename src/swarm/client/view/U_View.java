package swarm.client.view;

public class U_View
{
	public static double easeMantissa(double mantissa, double easing)
	{
		if( mantissa == 0.0 )
		{
			return 0;
		}
		else if( easing == 1.0)
		{
			return mantissa;
		}
		else
		{
			return Math.pow(mantissa, 1.0/easing);
		}
	}
}
