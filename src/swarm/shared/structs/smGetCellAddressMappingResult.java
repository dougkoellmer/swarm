package swarm.shared.structs;

import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smGetCellAddressMappingResult extends smA_JsonEncodable
{
	private smE_GetCellAddressMappingError m_error = smE_GetCellAddressMappingError.NO_ERROR;
	
	private smCellAddressMapping m_mapping = null;
	
	public smGetCellAddressMappingResult()
	{
	}
	
	public smGetCellAddressMappingResult(smE_GetCellAddressMappingError error)
	{
		setError(error);
	}
	
	public void setMapping(smCellAddressMapping mapping)
	{
		m_mapping = mapping;
		m_error = smE_GetCellAddressMappingError.NO_ERROR;
	}
	
	public smCellAddressMapping getMapping()
	{
		return m_mapping;
	}
	
	public smE_GetCellAddressMappingError getError()
	{
		return m_error;
	}
	
	public void setError(smE_GetCellAddressMappingError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == smE_GetCellAddressMappingError.NO_ERROR;
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		if( m_mapping != null && m_error == smE_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping.writeJson(null, json_out);
		}
		
		factory.getHelper().putEnum(json_out, smE_JsonKey.getCellAddressMappingError, m_error);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_error = factory.getHelper().getEnum(json, smE_JsonKey.getCellAddressMappingError, smE_GetCellAddressMappingError.values());
		
		if( m_error == smE_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping = new smCellAddressMapping();
			
			m_mapping.readJson(null, json);
		}
		else
		{
			m_mapping = null;
		}
	}

}
