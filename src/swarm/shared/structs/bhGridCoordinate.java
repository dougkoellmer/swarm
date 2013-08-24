package swarm.shared.structs; 

import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;


/**
 * ...
 * @author 
 */
public class bhGridCoordinate extends bhA_JsonEncodable
{
	private static final int HASH_CODE_SEED = 40000;
	private int m_m;
	private int m_n;

	public bhGridCoordinate()
	{
		set(0, 0);
	}
	
	public bhGridCoordinate(bhGridCoordinate source)
	{
		this.copy(source);
	}
	
	public bhGridCoordinate(int m, int n) 
	{
		set(m, n);
	}
	
	public int calcArrayIndex(int columnCount)
	{
		return m_m + m_n * columnCount;
	}
	
	public void incM(int delta)
	{
		m_m += delta;
	}
	
	public void incN(int delta)
	{
		m_n += delta;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( object instanceof bhGridCoordinate )
		{
			return this.isEqualTo((bhGridCoordinate) object);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		//--- DRK > This will technically start to collide if the grid ever gets larger than HASH_CODE_SEEDxHASH_CODE_SEED,
		//---		but I think collisions are impossible in any practical sense because the overlap should only happen
		//---		for coordinates on opposite sides of the grid (left/right), and one column down.  Even if this happened, it
		//---		would mean more than a billion active users, heh, so we would have bigger fish to fry.
		return m_m + m_n * HASH_CODE_SEED;
	}
	
	public void setComponent(int component, int value)
	{
		switch(component)
		{
			case 0:  m_m = value;  break;
			case 1:  m_n = value;  break;
		}
	}
	
	public boolean isEqualTo(bhGridCoordinate otherCoord)
	{
		return this.m_m == otherCoord.m_m && this.m_n == otherCoord.m_n;
	}
	
	public boolean isEqualTo(int m, int n)
	{
		return this.m_m == m && this.m_n == n;
	}
	
	public void setM(int m)
	{
		m_m = m;
	}
	
	public void setN(int n)
	{
		m_n = n;
	}
	
	public int getM()
	{
		return m_m;
	}
	
	public int getN()
	{
		return m_n;
	}
	
	public void set(int m, int n)
	{
		m_m = m;
		m_n = n;
	}
	
	public void copy(bhGridCoordinate otherCoordinate)
	{
		this.m_m = otherCoordinate.m_m;
		this.m_n = otherCoordinate.m_n;
	}
	
	public String writeString()
	{
		return m_m + "x" + m_n;
	}
	
	public void readString(String string)
	{
		String[] coordStrings = string.split("x");
		int m = Integer.parseInt(coordStrings[0]);
		int n = Integer.parseInt(coordStrings[1]);
		this.set(m, n);
	}
	
	
	public void setWithPoint(bhPoint point, double cellWidth, double cellHeight)
	{
		m_m = (int) (point.getX() / cellWidth);
		m_n = (int) (point.getY() / cellHeight);
		
		m_m = (int) Math.floor(m_m);
		m_n = (int) Math.floor(m_n);
	}
	
	public void calcPoint(bhPoint outPoint, double cellWidth, double cellHeight, int cellPadding, int subCellCount)
	{
		cellWidth = cellWidth * subCellCount;
		cellHeight = cellHeight * subCellCount;
		cellPadding = cellPadding * subCellCount;
		double x = m_m * cellWidth + ((m_m) * cellPadding);
		double y = m_n * cellHeight + ((m_n) * cellPadding);
		
		outPoint.set(x, y, 0.0);
	}
	
	public void calcCenterPoint(bhPoint outPoint, double cellWidth, double cellHeight, int cellPadding, int subCellCount)
	{
		cellWidth = cellWidth * subCellCount;
		cellHeight = cellHeight * subCellCount;
		
		calcPoint(outPoint, cellWidth, cellHeight, cellPadding, subCellCount);
		
		outPoint.inc(cellWidth/2.0, cellHeight/2.0, 0.0);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhA_JsonFactory jsonFactory = sm.jsonFactory;
		bhI_JsonArray components = jsonFactory.createJsonArray();
		components.addInt(getM());
		components.addInt(getN());
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.coordComponents, components);
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		bhI_JsonArray components = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.coordComponents);
		if( components != null )
		{
			for( int i = 0; i < 2 && i < components.getSize(); i++ )
			{
				this.setComponent(i, components.getInt(i));
			}
		}
	}
	
	@Override
	public boolean isEqualTo(bhI_JsonObject json)
	{
		bhI_JsonArray components = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.coordComponents);
		
		if( components == null )  return false;
		
		Integer m = components.getInt(0);
		Integer n = components.getInt(1);
		
		if( m != null && n != null )
		{
			return this.isEqualTo(m, n);
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isReadable(bhI_JsonObject json)
	{
		return sm.jsonFactory.getHelper().containsAllKeys(json, bhE_JsonKey.coordComponents);
	}
	
	@Override
	public String toString()
	{
		return "["+this.getClass().getName()+"(m="+m_m+", n="+m_n+")]";
	}
}