package swarm.shared.utils;

public final class smU_Math
{
	private smU_Math()
	{
		
	}
	
	public static boolean isWithin(double test, double low, double high)
	{
		double tolerance = 0;
		return test >= low-tolerance && test <= high+tolerance;
	}
	
	public static boolean equals( double var1, double var2, double tolerance)
	{
		return isWithin(var1, var2 - tolerance, var2 + tolerance);
	}
	
	public static double sign(double value)
	{
		return value / (value != 0 ? Math.abs(value) : 1);
	}

	public static double getRandomSign()
	{
		return Math.random() > .5 ? 1 : -1;
	}
	
	public static double clamp(double value, double lowerLimit, double upperLimit)
	{
		return value < lowerLimit ? lowerLimit : (value > upperLimit ? upperLimit : value);
	}
}
