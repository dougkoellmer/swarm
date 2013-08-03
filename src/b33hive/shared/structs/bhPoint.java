package b33hive.shared.structs;


/**
 * ...
 * @author 
 */
public class bhPoint extends bhA_Coordinate
{
	public bhPoint(double x, double y, double z)
	{
		super(x, y, z);
	}
	
	public bhPoint() 
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
	public boolean isEqualTo(bhA_Coordinate otherEntity, bhTolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : bhTolerance.DEFAULT;
		
		if ( otherEntity instanceof bhPoint )
		{
			return this.calcDistanceTo(((bhPoint)otherEntity)) <= tolerance.equalPoint;
		}
		
		return false;
	}
	
	public void calcDifference(bhPoint otherPoint, bhVector outVector)
	{
		outVector.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
	}

	public bhVector minus(bhPoint otherPoint)
	{
		bhVector vec = new bhVector();
		vec.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
		return vec;
	}
	
	public double calcDistanceTo(bhPoint otherPoint)
	{
		return otherPoint.minus(this).calcLength();
	}

	public void calcMidwayPoint(bhPoint otherPoint, bhPoint outPoint)
	{
		bhVector translater = otherPoint.minus(this);
		translater.scaleByNumber(.5);
		outPoint.copy(this);
		outPoint.translate(translater);
	}

	public double calcManhattanDistanceTo(bhPoint otherPoint)
	{
		return Math.abs(otherPoint.getX() - this.getX()) + Math.abs(otherPoint.getY() - this.getY()) + Math.abs(otherPoint.getZ() - this.getZ());
	}
}