package b33hive.shared.structs;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.app.bh;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhGetCellAddressMappingResult extends bhA_JsonEncodable
{
	private bhE_GetCellAddressMappingError m_error = bhE_GetCellAddressMappingError.NO_ERROR;
	
	private bhCellAddressMapping m_mapping = null;
	
	public bhGetCellAddressMappingResult()
	{
	}
	
	public bhGetCellAddressMappingResult(bhE_GetCellAddressMappingError error)
	{
		setError(error);
	}
	
	public void setMapping(bhCellAddressMapping mapping)
	{
		m_mapping = mapping;
		m_error = bhE_GetCellAddressMappingError.NO_ERROR;
	}
	
	public bhCellAddressMapping getMapping()
	{
		return m_mapping;
	}
	
	public bhE_GetCellAddressMappingError getError()
	{
		return m_error;
	}
	
	public void setError(bhE_GetCellAddressMappingError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == bhE_GetCellAddressMappingError.NO_ERROR;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_mapping != null && m_error == bhE_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping.writeJson(json);
		}
		
		bh.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.getCellAddressMappingError, m_error);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_error = bh.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.getCellAddressMappingError, bhE_GetCellAddressMappingError.values());
		
		if( m_error == bhE_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping = new bhCellAddressMapping();
			
			m_mapping.readJson(json);
		}
		else
		{
			m_mapping = null;
		}
	}

}
