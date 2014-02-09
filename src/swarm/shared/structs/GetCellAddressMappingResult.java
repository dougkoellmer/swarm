package swarm.shared.structs;

import swarm.shared.json.A_JsonEncodable;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class GetCellAddressMappingResult extends A_JsonEncodable
{
	private E_GetCellAddressMappingError m_error = E_GetCellAddressMappingError.NO_ERROR;
	
	private CellAddressMapping m_mapping = null;
	
	public GetCellAddressMappingResult()
	{
	}
	
	public GetCellAddressMappingResult(E_GetCellAddressMappingError error)
	{
		setError(error);
	}
	
	public GetCellAddressMappingResult(CellAddressMapping mapping, E_GetCellAddressMappingError error)
	{
		setMapping(mapping);
		setError(error);
	}
	
	public void setMapping(CellAddressMapping mapping)
	{
		m_mapping = mapping;
		m_error = E_GetCellAddressMappingError.NO_ERROR;
	}
	
	public CellAddressMapping getMapping()
	{
		return m_mapping;
	}
	
	public E_GetCellAddressMappingError getError()
	{
		return m_error;
	}
	
	public void setError(E_GetCellAddressMappingError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == E_GetCellAddressMappingError.NO_ERROR;
	}
	
	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		if( m_mapping != null && m_error == E_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping.writeJson(json_out, factory);
		}
		
		factory.getHelper().putEnum(json_out, E_JsonKey.getCellAddressMappingError, m_error);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_error = factory.getHelper().getEnum(json, E_JsonKey.getCellAddressMappingError, E_GetCellAddressMappingError.values());
		
		if( m_error == E_GetCellAddressMappingError.NO_ERROR )
		{
			m_mapping = new CellAddressMapping();
			
			m_mapping.readJson(json, factory);
		}
		else
		{
			m_mapping = null;
		}
	}

}
