package swarm.shared.structs; 

import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonComparable;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;


/**
 * ...
 * @author 
 */
public class GridCoordinate extends A_JsonEncodable implements I_JsonComparable
{
	private static final int HASH_CODE_SEED = 40000;
	private int m_m;
	private int m_n;

	public GridCoordinate()
	{
		set(0, 0);
	}
	
	public GridCoordinate(A_JsonFactory jsonFactory, I_JsonObject json)
	{	
		super(jsonFactory, json);
	}
	
	public GridCoordinate(GridCoordinate source)
	{
		this.copy(source);
	}
	
	public GridCoordinate(int m, int n) 
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
		if( object instanceof GridCoordinate )
		{
			return this.isEqualTo((GridCoordinate) object);
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
	
	public boolean isEqualTo(GridCoordinate otherCoord)
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
	
	public void copy(GridCoordinate otherCoordinate)
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
		this.readStrings(coordStrings);
	}
	
	public void readStrings(String[] coords)
	{
		int m = Integer.parseInt(coords[0]);
		int n = Integer.parseInt(coords[1]);
		this.set(m, n);
	}
	
	
	public void setWithPoint(Point point, double cellWidth, double cellHeight)
	{
		m_m = (int) (point.getX() / cellWidth);
		m_n = (int) (point.getY() / cellHeight);
		
		m_m = (int) Math.floor(m_m);
		m_n = (int) Math.floor(m_n);
	}
	
	public void calcPoint(Point outPoint, double cellWidth, double cellHeight, int cellPadding, int subCellCount)
	{
		cellWidth = cellWidth * subCellCount;
		cellHeight = cellHeight * subCellCount;
		cellPadding = cellPadding * subCellCount;
		double x = m_m * cellWidth + ((m_m) * cellPadding);
		double y = m_n * cellHeight + ((m_n) * cellPadding);
		
		outPoint.set(x, y, 0.0);
	}
	
	public void calcCenterPoint(Point outPoint, double cellWidth, double cellHeight, int cellPadding, int subCellCount)
	{		
		calcPoint(outPoint, cellWidth, cellHeight, cellPadding, subCellCount);
		
		cellWidth = cellWidth * subCellCount;
		cellHeight = cellHeight * subCellCount;
		
		outPoint.inc(cellWidth/2.0, cellHeight/2.0, 0.0);
	}
	
	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		I_JsonArray components = factory.createJsonArray();
		components.addInt(getM());
		components.addInt(getN());
		factory.getHelper().putJsonArray(json_out, E_JsonKey.coordComponents, components);
	}
	
	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		I_JsonArray components = factory.getHelper().getJsonArray(json, E_JsonKey.coordComponents);
		if( components != null )
		{
			for( int i = 0; i < 2 && i < components.getSize(); i++ )
			{
				this.setComponent(i, components.getInt(i));
			}
		}
	}
	
	@Override
	public boolean isEqualTo(A_JsonFactory factory, I_JsonObject json)
	{
		I_JsonArray components = factory.getHelper().getJsonArray(json, E_JsonKey.coordComponents);
		
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
	
	public static boolean isReadable(A_JsonFactory factory, I_JsonObject json)
	{
		return factory.getHelper().containsAllKeys(json, E_JsonKey.coordComponents);
	}
	
	@Override
	public String toString()
	{
		return "["+this.getClass().getName()+"(m="+m_m+", n="+m_n+")]";
	}
}