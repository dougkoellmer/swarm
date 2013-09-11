package swarm.shared.structs;

import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonComparable;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCellAddressMapping extends smA_JsonEncodable implements smI_JsonComparable
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
	
	public smCellAddressMapping(smCellAddressMapping mapping)
	{
		this.copy(mapping);
	}
	
	public void copy(smCellAddressMapping mapping)
	{
		this.m_coordinate.copy(mapping.getCoordinate());
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
	public boolean isEqualTo(smA_JsonFactory factory, smI_JsonObject json)
	{
		return m_coordinate.isEqualTo(null, json);
	}
	
	public boolean isEqualTo(smCellAddressMapping otherMapping)
	{
		return this.m_coordinate.isEqualTo(otherMapping.m_coordinate);
	}
	
	public static boolean isReadable(smA_JsonFactory factory, smI_JsonObject json)
	{
		return smGridCoordinate.isReadable(factory, json);
	}
	
	public String writeString()
	{
		return m_coordinate.writeString();
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		m_coordinate.writeJson(factory, json_out);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_coordinate.readJson(factory, json);
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