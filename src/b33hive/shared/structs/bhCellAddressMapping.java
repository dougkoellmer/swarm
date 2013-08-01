package com.b33hive.shared.structs;

import com.b33hive.shared.json.bhA_JsonEncodable;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;

public class bhCellAddressMapping extends bhA_JsonEncodable
{
	protected bhGridCoordinate m_coordinate = null;
	
	public bhCellAddressMapping()
	{
		initCoordinate();
	}
	
	public bhCellAddressMapping(bhGridCoordinate sourceCoordinate)
	{
		initCoordinate();
		
		m_coordinate.copy(sourceCoordinate);
	}
	
	protected void initCoordinate()
	{
		m_coordinate = new bhGridCoordinate();
	}
	
	public bhGridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	@Override
	public boolean isEqualTo(bhI_JsonObject json)
	{
		return m_coordinate.isEqualTo(json);
	}
	
	public boolean isEqualTo(bhCellAddressMapping otherMapping)
	{
		return this.m_coordinate.isEqualTo(otherMapping.m_coordinate);
	}
	
	public static boolean isReadable(bhI_JsonObject json)
	{
		return bhGridCoordinate.isReadable(json);
	}
	
	public String writeString()
	{
		return m_coordinate.writeString();
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		m_coordinate.writeJson(json);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_coordinate.readJson(json);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( object instanceof bhCellAddressMapping )
		{
			return this.isEqualTo((bhCellAddressMapping) object);
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