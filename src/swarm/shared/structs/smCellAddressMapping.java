package swarm.shared.structs;

import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCellAddressMapping extends smA_JsonEncodable
{
	protected smGridCoordinate m_coordinate = null;
	
	public smCellAddressMapping()
	{
		initCoordinate();
	}
	
	public smCellAddressMapping(smGridCoordinate sourceCoordinate)
	{
		initCoordinate();
		
		m_coordinate.copy(sourceCoordinate);
	}
	
	protected void initCoordinate()
	{
		m_coordinate = new smGridCoordinate();
	}
	
	public smGridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	@Override
	public boolean isEqualTo(smI_JsonObject json)
	{
		return m_coordinate.isEqualTo(json);
	}
	
	public boolean isEqualTo(smCellAddressMapping otherMapping)
	{
		return this.m_coordinate.isEqualTo(otherMapping.m_coordinate);
	}
	
	public static boolean isReadable(smI_JsonObject json)
	{
		return smGridCoordinate.isReadable(json);
	}
	
	public String writeString()
	{
		return m_coordinate.writeString();
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		m_coordinate.writeJson(json);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		m_coordinate.readJson(json);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( object instanceof smCellAddressMapping )
		{
			return this.isEqualTo((smCellAddressMapping) object);
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