package swarm.shared.structs;

import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonComparable;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class CellAddressMapping extends A_JsonEncodable implements I_JsonComparable
{
	protected GridCoordinate m_coordinate = null;
	
	public CellAddressMapping()
	{
		initCoordinate();
	}
	
	public CellAddressMapping(GridCoordinate sourceCoordinate)
	{
		initCoordinate();
		
		m_coordinate.copy(sourceCoordinate);
	}
	
	public CellAddressMapping(CellAddressMapping mapping)
	{
		initCoordinate();
		
		this.copy(mapping);
	}
	
	public void copy(CellAddressMapping mapping)
	{
		this.m_coordinate.copy(mapping.getCoordinate());
	}
	
	protected void initCoordinate()
	{
		m_coordinate = new GridCoordinate();
	}
	
	public GridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	@Override
	public boolean isEqualTo(A_JsonFactory factory, I_JsonObject json)
	{
		return m_coordinate.isEqualTo(factory, json);
	}
	
	public boolean isEqualTo(CellAddressMapping otherMapping)
	{
		return this.m_coordinate.isEqualTo(otherMapping.m_coordinate);
	}
	
	public static boolean isReadable(A_JsonFactory factory, I_JsonObject json)
	{
		return GridCoordinate.isReadable(factory, json);
	}
	
	public String writeString()
	{
		return m_coordinate.writeString();
	}

	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		m_coordinate.writeJson(json_out, factory);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_coordinate.readJson(json, factory);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( object instanceof CellAddressMapping )
		{
			return this.isEqualTo((CellAddressMapping) object);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return m_coordinate.hashCode();
	}
	
	@Override
	public String toString()
	{
		return m_coordinate.toString();
	}
}