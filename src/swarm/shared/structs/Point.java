package swarm.shared.structs;


/**
 * ...
 * @author 
 */
public class Point extends A_Coordinate
{
	public Point(double x, double y, double z)
	{
		super(x, y, z);
	}
	
	public Point() 
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
	public boolean isEqualTo(A_Coordinate otherEntity, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		if ( otherEntity instanceof Point )
		{
			return this.calcDistanceTo(((Point)otherEntity)) <= tolerance.equalPoint;
		}
		
		return false;
	}
	
	public void calcDifference(Point otherPoint, Vector outVector)
	{
		outVector.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
	}

	public Vector minus(Point otherPoint)
	{
		Vector vec = new Vector();
		vec.set(getX() - otherPoint.getX(), getY() - otherPoint.getY(), getZ() - otherPoint.getZ());
		return vec;
	}
	
	public double calcDistanceTo(Point otherPoint)
	{
		return otherPoint.minus(this).calcLength();
	}

	public void calcMidwayPoint(Point otherPoint, Point outPoint)
	{
		Vector translater = otherPoint.minus(this);
		translater.scaleByNumber(.5);
		outPoint.copy(this);
		outPoint.translate(translater);
	}

	public double calcManhattanDistanceTo(Point otherPoint)
	{
		return Math.abs(otherPoint.getX() - this.getX()) + Math.abs(otherPoint.getY() - this.getY()) + Math.abs(otherPoint.getZ() - this.getZ());
	}
}