package swarm.shared.structs;

public class bhTolerance
{
	public static final bhTolerance DEFAULT = new bhTolerance();
	public static final bhTolerance EXACT = new bhTolerance(0, 0, 0, 0);
	
	private static final double DEFAULT_VALUE = .00000001;
	
	public double equalPoint = Double.NaN;
	public double equalVector = Double.NaN;
	public double equalAngle = Double.NaN;
	public double equalComponent = Double.NaN;
	
	public bhTolerance(bhTolerance source)
	{
		this.copy(source);
	}
	
	public bhTolerance(double equalPoint, double equalVector, double equalAngle, double equalComponent) 
	{
		this.equalPoint = Double.isNaN(equalPoint) ? DEFAULT_VALUE : equalPoint;
		this.equalVector = Double.isNaN(equalVector) ? DEFAULT_VALUE : equalVector;
		this.equalAngle = Double.isNaN(equalAngle) ? DEFAULT_VALUE : equalAngle;
		this.equalComponent = Double.isNaN(equalComponent) ? DEFAULT_VALUE : equalComponent;
	}
	
	public bhTolerance() 
	{
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}
	
	public void copy(bhTolerance otherTolerance)
	{
		this.equalPoint = otherTolerance.equalPoint;
		this.equalVector = otherTolerance.equalVector;
		this.equalAngle = otherTolerance.equalAngle;
		this.equalComponent = otherTolerance.equalComponent;
	}
}
