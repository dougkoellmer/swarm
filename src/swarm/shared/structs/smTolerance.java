package swarm.shared.structs;

public class smTolerance
{
	public static final smTolerance DEFAULT = new smTolerance();
	public static final smTolerance EXACT = new smTolerance(0, 0, 0, 0);
	
	private static final double DEFAULT_VALUE = .00000001;
	
	public double equalPoint = Double.NaN;
	public double equalVector = Double.NaN;
	public double equalAngle = Double.NaN;
	public double equalComponent = Double.NaN;
	
	public smTolerance(smTolerance source)
	{
		this.copy(source);
	}
	
	public smTolerance(double equalPoint, double equalVector, double equalAngle, double equalComponent) 
	{
		this.equalPoint = Double.isNaN(equalPoint) ? DEFAULT_VALUE : equalPoint;
		this.equalVector = Double.isNaN(equalVector) ? DEFAULT_VALUE : equalVector;
		this.equalAngle = Double.isNaN(equalAngle) ? DEFAULT_VALUE : equalAngle;
		this.equalComponent = Double.isNaN(equalComponent) ? DEFAULT_VALUE : equalComponent;
	}
	
	public smTolerance() 
	{
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}
	
	public void copy(smTolerance otherTolerance)
	{
		this.equalPoint = otherTolerance.equalPoint;
		this.equalVector = otherTolerance.equalVector;
		this.equalAngle = otherTolerance.equalAngle;
		this.equalComponent = otherTolerance.equalComponent;
	}
}
