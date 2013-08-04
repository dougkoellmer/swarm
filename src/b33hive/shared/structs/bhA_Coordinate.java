package b33hive.shared.structs;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.utils.bhU_Singletons;
import b33hive.shared.debugging.bhU_Logging;

public abstract class bhA_Coordinate extends bhA_JsonEncodable
{
	private double m_x = 0, m_y = 0, m_z = 0;
	
	public bhA_Coordinate(double x, double y, double z) 
	{
		set(x, y, z);
	}
	
	public void set(double x, double y, double z)
	{
		m_x = x;
		m_y = y;
		m_z = z;
	}
	
	public double getComponent(int index)
	{
		switch(index)
		{
			case 0:  return m_x;
			case 1:  return m_y;
			case 2:  return m_z;
		}
		
		return Double.NaN;
	}
	
	public void setComponent(int index, double value)
	{
		switch(index)
		{
			case 0:  m_x = value;  break;
			case 1:  m_y = value;  break;
			case 2:  m_z = value;  break;
		}
	}
	
	public void zeroOut()
	{
		set(0, 0, 0);
	}
	
	public boolean isEqualTo(bhA_Coordinate otherEntity, bhTolerance tolerance)
	{
		tolerance = tolerance != null ? tolerance : bhTolerance.DEFAULT;
		
		bhA_Coordinate asCoord = (bhA_Coordinate) otherEntity;
		if ( asCoord != null )
		{
			//if( otherEntity
		}
		
		return false;
	}
	
	public double getX()
	{
		return m_x;
	}
	public void setX(double value)
	{
		set(value, getY(), getZ());
	}
	
	public double getY()
	{
		return m_y;
	}
	
	public void setY(double value)
	{
		set(getX(), value, getZ());
	}
	
	public double getZ()
	{
		return m_z;
	}
	
	public void setZ(double value)
	{
		set(getX(), getY(), value);
	}
	
	public void inc( double xDelta, double yDelta, double zDelta )
	{
		set(getX() + xDelta, getY() + yDelta, getZ() + zDelta);
	}
		
	public void incX(double value)
	{
		inc(value, 0, 0);
	}
	
	public void incY(double value)
	{
		inc(0, value, 0);
	}
	
	public void incZ(double value)
	{
		inc(0, 0, value);
	}
	
	public void add(bhA_Coordinate otherCoordinate)
	{
		inc(otherCoordinate.getX(), otherCoordinate.getY(), otherCoordinate.getZ());
	}
	
	public void subtract(bhA_Coordinate otherCoordinate)
	{
		inc(-otherCoordinate.getX(), -otherCoordinate.getY(), -otherCoordinate.getZ());
	}
	
	public void copy(Object otherObject)
	{		
		if ( otherObject instanceof bhA_Coordinate )
		{
			bhA_Coordinate otherCoordinate = (bhA_Coordinate) otherObject;
		
			this.set(otherCoordinate.getX(), otherCoordinate.getY(), otherCoordinate.getZ());
		}
	}
	
	public void scaleByNumber(double value)
	{
		this.set(getX() * value, getY() * value, getZ() * value);
	}
	
	public void scale(bhVector vector, bhPoint origin)
	{
		//include "../../QB2_MATH_PUSH_DISPATCHING_BLOCK";
		
		if ( origin != null)
		{
			bhVector vec = (new bhPoint()).minus(origin);
			this.translate(vec);
			this.set(getX() * vector.getX(), getY() * vector.getY(), getZ() * vector.getZ());
			vec.negate();
			this.translate(vec);
		}
		else
		{
			this.set(getX() * vector.getX(), getY() * vector.getY(), getZ() * vector.getZ());
		}
		
		//this.dispatchUpdatedEvent();
		//include "../../QB2_MATH_POP_DISPATCHING_BLOCK";
	}

	public void rotate(double radians, Object axis)
	{
		bhPoint axis2d = (bhPoint) axis;
		
		double originX, originY;//, originZ;
		if ( axis2d != null )
		{
			originX = axis2d.getX();
			originY = axis2d.getY();
		///	originZ = axis2d.getZ();
		}
		else
		{
			originX = 0;
			originY = 0;
		//	originZ = 0;
		}
		
		final double sinRad = Math.sin(radians);
		final double cosRad = Math.cos(radians);
		final double newVertX = originX + cosRad * (this.getX() - originX) - sinRad * (this.getY() - originY);
		final double newVertY = originY + sinRad * (this.getX() - originX) + cosRad * (this.getY() - originY);
		
		this.set(newVertX, newVertY, 0);
	}
	
	public void translate(bhVector vector)
	{
		this.add(vector);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhA_JsonFactory jsonFactory = bhU_Singletons.get(bhA_JsonFactory.class);
		bhI_JsonArray components = jsonFactory.createJsonArray();
		components.addDouble(getX());
		components.addDouble(getY());
		components.addDouble(getZ());
		bhJsonHelper.getInstance().putJsonArray(json, bhE_JsonKey.pointComponents, components);
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		bhI_JsonArray components = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.pointComponents);
		if( components != null )
		{
			for( int i = 0; i < 3 && i < components.getSize(); i++ )
			{
				this.setComponent(i, components.getDouble(i));
			}
		}
	}
	
	public static boolean isReadable(bhI_JsonObject json)
	{
		return bhJsonHelper.getInstance().containsAllKeys(json, bhE_JsonKey.pointComponents);
	}
	
	@Override
	public String toString()
	{
		return "["+this.getClass().getName()+"(x="+bhU_Logging.toFixed(m_x)+", y="+bhU_Logging.toFixed(m_y)+", z="+bhU_Logging.toFixed(m_z)+"]";
	}
}
