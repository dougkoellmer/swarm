package swarm.shared.structs;

import swarm.shared.json.A_JsonEncodable;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class GetCellAddressResult extends A_JsonEncodable
{
	private E_GetCellAddressError m_error = E_GetCellAddressError.NO_ERROR;
	
	private CellAddress m_address;
	
	public GetCellAddressResult()
	{
	}
	
	public GetCellAddressResult(E_GetCellAddressError error)
	{
		setError(error);
	}
	
	public void setAddress(CellAddress address)
	{
		m_address = address;
	}
	
	public CellAddress getAddress()
	{
		return m_address;
	}
	
	public E_GetCellAddressError getError()
	{
		return m_error;
	}
	
	public void setError(E_GetCellAddressError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == E_GetCellAddressError.NO_ERROR;
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		if( m_address != null && m_error == E_GetCellAddressError.NO_ERROR )
		{
			m_address.writeJson(factory, json_out);
		}
		
		factory.getHelper().putEnum(json_out, E_JsonKey.getCellAddressError, m_error);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_error = factory.getHelper().getEnum(json, E_JsonKey.getCellAddressError, E_GetCellAddressError.values());
		
		if( m_error == E_GetCellAddressError.NO_ERROR )
		{
			m_address = new CellAddress();
			
			m_address.readJson(factory, json);
		}
		else
		{
			m_address = null;
		}
	}

}
