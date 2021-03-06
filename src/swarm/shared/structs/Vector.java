package swarm.shared.structs; 

import swarm.shared.utils.U_Math;
import swarm.shared.debugging.U_Debug;



/**
 * ...
 * @author 
 */
public class Vector extends A_Coordinate
{
	public Vector(double x, double y, double z) 
	{
		super(x, y, z);
	}
	
	public Vector() 
	{
		super(0, 0, 0);
	}
	
	public static Vector newRotVector(double baseX, double baseY, double radians)
	{
		Vector newVec = new Vector(baseX, baseY, 0);
		newVec.rotate(radians, null);
		return newVec;
	}
	
	@Override
	public boolean isEqualTo(A_Coordinate otherEntity, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
	
		U_Debug.ASSERT(false);
		
		/*if ( qb2_is(otherEntity, smVector) )
		{
			return false;
		}*/
		
		return super.isEqualTo(otherEntity, tolerance);
	}
	
	public boolean isZeroLength(Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.EXACT;
		
		return U_Math.equals(this.calcLengthSquared(), 0, tolerance.equalPoint);
	}

	public void calcNormal(Vector outVector)
	{
		double mag = this.calcLength();
		outVector.set(getX() / mag, getY() / mag, getZ() / mag);
	}

	public void normalize()
	{
		double mag = this.calcLength();
		if ( mag != 0)
		{
			this.set(getX() / mag, getY() / mag, getZ() / mag);
		}
	}

	public double calcDotProduct(Vector otherVector)
	{
		return getX() * otherVector.getX() + getY() * otherVector.getY() + getZ() * otherVector.getZ();
	}

	/*public void calcPerpVector(eDirection:qb2E_PerpVectorDirection, outVector:smVector)
	{
		outVector.setToPerpVector(eDirection);
	}
		
	public void setToPerpVector(eDirection:qb2E_PerpVectorDirection)
	{
		var direction:int = eDirection == qb2E_PerpVectorDirection.RIGHT ? 1 : -1;
		
		//--- Because in flash and many other 2d graphics rendering systems the y-axis is flipped, flip the direction internally.
		direction = -direction;
		
		var tempX:Number = getX();
		var tempY:Number = getY();
		
		if ( direction >= 0 )
		{
			getY() = -tempX;
			getX() = tempY;
			
			set(tempY, -tempX);
		}
		else
		{
			set(-tempY, tempX);
		}
	}*/
	
	public boolean isNaNVector()
	{
		return Double.isNaN(getX()) || Double.isNaN(getY()) || Double.isNaN(getZ());
	}
	
	public boolean isUnitLength(Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		return U_Math.equals(1.0, this.calcLength(), tolerance.equalVector);
	}

	public boolean isCodirectionalTo(Vector otherVector, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		return this.calcAngleTo(otherVector) <= tolerance.equalAngle;
	}

	public boolean isAntidirectionalTo(Vector otherVector, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		return this.calcAngleTo(otherVector) >= Math.PI - tolerance.equalAngle;
	}
		
	public boolean isParallelTo(Vector otherVector, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		double angle = this.calcAngleTo(otherVector);
		return angle <= tolerance.equalAngle || angle >= Math.PI - tolerance.equalAngle;
	}
	
	public boolean isPerpendicularTo(Vector otherVector, Tolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : Tolerance.DEFAULT;
		
		return U_Math.equals(this.calcAngleTo(otherVector), Math.PI / 2, tolerance.equalAngle);
	}

	/*public override function mirror(plane:qb2I_GeoHyperPlane)
	{
		var line:qb2GeoLine = plane as qb2GeoLine;
		var lineNormal:smVector = new smVector();
		line.calcDirection(lineNormal, true);
		var dot:Number = -getX() * lineNormal.getX() - getY() * lineNormal.getY();
		getX() = getX() + 2 * lineNormal.getX() * dot;
		getY() = getY() + 2 * lineNormal.getY() * dot;
		negate();
	}*/

	public void negate()
	{
		this.scaleByNumber(-1);
	}
	
	public double calcSignedAngleTo(Vector otherVector)
	{
		return Math.atan2(otherVector.getY(), otherVector.getX()) - Math.atan2(getY(), getX());
	}

	public double calcClockwiseAngleTo(Vector otherVector)
	{
		double signedAngle = calcSignedAngleTo(otherVector);
		return signedAngle < 0 ? Math.PI + (Math.PI + signedAngle) : signedAngle;
	}
	
	public double calcAngleTo(Vector otherVector)
	{
		Vector thisNormal = new Vector();
		this.calcNormal(thisNormal);
		Vector otherNormal = new Vector();
		otherVector.calcNormal(otherNormal);
		return Math.acos(thisNormal.calcDotProduct(otherNormal));
	}
	
	public double calcLength()
	{
		return Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
	}
	
	public void setLength(double value)
	{
		//normalize() is not used so that sendCallbacks is not called twice.
		double mag = this.calcLength();
		if ( mag != 0 )
		{
			set(this.getX() / mag, getY() / mag, getZ() / mag);
		}
		
		this.scaleByNumber(value);
	}
	
	public double calcLengthSquared()
	{
		return getX() * getX() + getY() * getY() + getZ() * getZ();
	}

	
	/*public override function draw(graphics:qb2I_Graphics2d, base:qb2GeoPoint, baseRadius:Number = 0, arrowSize:Number = 5, scale:Number = 1, makeBaseTheEndOfTheVector = false )
	{
		var beg:qb2GeoPoint, end:qb2GeoPoint;
		var vec:smVector = scale == 1 ? this : this.scaledBy(scale);
		if ( makeBaseTheEndOfTheVector )
		{
			beg = base.translatedBy(vec.negated());
			end = base;
		}
		else
		{
			beg = base;
			end = base.translatedBy(vec);
		}
		graphics.drawLine(beg, end);
		beg.draw(graphics, baseRadius);
		end.drawAsArrow(graphics, this, arrowSize);
	}*/
	
}