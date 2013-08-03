package b33hive.shared.structs;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhGetCellAddressResult extends bhA_JsonEncodable
{
	private bhE_GetCellAddressError m_error = bhE_GetCellAddressError.NO_ERROR;
	
	private bhCellAddress m_address;
	
	public bhGetCellAddressResult()
	{
	}
	
	public bhGetCellAddressResult(bhE_GetCellAddressError error)
	{
		setError(error);
	}
	
	public void setAddress(bhCellAddress address)
	{
		m_address = address;
	}
	
	public bhCellAddress getAddress()
	{
		return m_address;
	}
	
	public bhE_GetCellAddressError getError()
	{
		return m_error;
	}
	
	public void setError(bhE_GetCellAddressError error)
	{
		m_error = error;
	}
	
	public boolean isEverythingOk()
	{
		return m_error == bhE_GetCellAddressError.NO_ERROR;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_address != null && m_error == bhE_GetCellAddressError.NO_ERROR )
		{
			m_address.writeJson(json);
		}
		
		bhJsonHelper.getInstance().putEnum(json, bhE_JsonKey.getCellAddressError, m_error);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_error = bhJsonHelper.getInstance().getEnum(json, bhE_JsonKey.getCellAddressError, bhE_GetCellAddressError.values());
		
		if( m_error == bhE_GetCellAddressError.NO_ERROR )
		{
			m_address = new bhCellAddress();
			
			m_address.readJson(json);
		}
		else
		{
			m_address = null;
		}
	}

}
