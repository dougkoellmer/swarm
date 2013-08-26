package swarm.shared.structs;


/**
 * ...
 * @author 
 */
public class smPoint extends smA_Coordinate
{
	public smPoint(double x, double y, double z)
	{
		super(x, y, z);
	}
	
	public smPoint() 
	{
		super(0, 0, 0);
	}
	
	public void round()
	{
		set(Math.round(getX()), Math.round(getY()), Math.round(getZ()));
	}
	
	public void floor()
	{
		set(Math.floor(getX()), Math.floor(getY()), Math.floor(getZ()));
	}
	
	@Override
	public boolean isEqualTo(smA_Coordinate otherEntity, smTolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : bhTolerance.DEFAULT;
		
		if ( otherEntity instanceof bhPoint )
		{
			return this.calcDistanceTo(((smPoint)otherEntity)) <= tolerance.equalPoint;
		}
		
		return false;
	}
	
	public void calcDifference(smPoint otherPoint, smVector outVector)
	{
		outVector.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
	}

	public smVector minus(smPoint otherPoint)
	{
		bhVector vec = new smVector();
		vec.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
		return vec;
	}
	
	public double calcDistanceTo(smPoint otherPoint)
	{
		return otherPoint.minus(this).calcLength();
	}

	public void calcMidwayPoint(smPoint otherPoint, smPoint outPoint)
	{
		bhVector translater = otherPoint.minus(this);
		translater.scaleByNumber(.5);
		outPoint.copy(this);
		outPoint.translate(translater);
	}

	public double calcManhattanDistanceTo(smPoint otherPoint)
	{
		return Math.abs(otherPoint.getX() - this.getX()) + Math.abs(otherPoint.getY() - this.getY()) + Math.abs(otherPoint.getZ() - this.getZ());
	}
}