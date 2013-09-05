package swarm.shared.structs;

import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smGetCellAddressResult extends smA_JsonEncodable
{
	private smE_GetCellAddressError m_error = smE_GetCellAddressError.NO_ERROR;
	
	private smCellAddress m_address;
	
	public smGetCellAddressResult()
	{
	}
	
	public smGetCellAddressResult(smE_GetCellAddressError error)
	{
		setError(error);
	}
	
	public void setAddress(smCellAddress address)
	{
		m_address = address;
	}
	
	public smCellAddress getAddress()
	{
		return m_address;
	}
	
	public smE_GetCellAddressError getError()
	{
		return m_error;
	}
	
	public void setError(smE_GetCellAddressError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == smE_GetCellAddressError.NO_ERROR;
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		if( m_address != null && m_error == smE_GetCellAddressError.NO_ERROR )
		{
			m_address.writeJson(factory, json_out);
		}
		
		factory.getHelper().putEnum(json_out, smE_JsonKey.getCellAddressError, m_error);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_error = factory.getHelper().getEnum(json, smE_JsonKey.getCellAddressError, smE_GetCellAddressError.values());
		
		if( m_error == smE_GetCellAddressError.NO_ERROR )
		{
			m_address = new smCellAddress();
			
			m_address.readJson(factory, json);
		}
		else
		{
			m_address = null;
		}
	}

}
