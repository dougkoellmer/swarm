package b33hive.shared.structs;

import java.util.ArrayList;

import b33hive.shared.app.bhS_App;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhI_SignUpCredentialValidator;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhCellAddress extends bhA_JsonEncodable
{
	public static enum E_Part
	{
		USERNAME,
		CELL
	}
	
	private String m_rawAddressNoLeadSlash = null;
	private String m_rawAddress = null;
	private final String[] m_parts = new String[E_Part.values().length];
	
	private bhE_CellAddressParseError m_cachedParseError = null;
	
	public bhCellAddress()
	{
		init(null);
	}
	
	public bhCellAddress(String rawAddress)
	{
		init(rawAddress);
	}
	
	public bhCellAddress(bhI_JsonObject json)
	{
		init(null);
		
		this.readJson(json);
	}
	
	public bhCellAddress(bhCellAddress source)
	{
		this(source.getRawAddress());
	}
	
	public boolean isValid()
	{
		return this.getParseError() == bhE_CellAddressParseError.NO_ERROR;
	}
	
	protected void init(String rawAddress)
	{
		m_rawAddressNoLeadSlash = null;
		m_rawAddress = null;
		m_cachedParseError = null;
		for( int i = 0; i < m_parts.length; i++ )
		{
			m_parts[i] = null;
		}
		
		parse(rawAddress);
	}
	
	public bhE_CellAddressParseError getParseError()
	{
		return m_cachedParseError;
	}
	
	public String getRawAddress()
	{
		return m_rawAddress;
	}
	
	public String getRawAddressNoLeadSlash()
	{
		return m_rawAddressNoLeadSlash;
	}
	
	public String getPart(E_Part part)
	{
		return m_parts[part.ordinal()];
	}
	
	private void parse(String rawAddress)
	{
		m_rawAddressNoLeadSlash = null;
		m_rawAddress = null;
		
		if( rawAddress == null || rawAddress.length() == 0 )
		{
			m_cachedParseError = bhE_CellAddressParseError.EMPTY;
			
			return;
		}
		
		//--- DRK > For some reason, the split function below won't recognize multiple trailing slashes, only leading slashes.
		//---		So we just account for that strange case simply here.
		if( rawAddress.contains("//") )
		{
			m_cachedParseError = bhE_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		//--- DRK > Strip off leading and trailing slashes.
		String rawAddressFiltered = rawAddress.startsWith("/") ? rawAddress.replaceFirst("/", "") : rawAddress;
		rawAddressFiltered = rawAddressFiltered.endsWith("/") ? rawAddressFiltered.substring(0, rawAddressFiltered.length()-1) : rawAddressFiltered;
		
		if( rawAddressFiltered == null || rawAddressFiltered.length() == 0 )
		{
			m_cachedParseError = bhE_CellAddressParseError.EMPTY;
			
			return;
		}
		
		rawAddressFiltered = rawAddressFiltered.toLowerCase();
		
		String[] parts = rawAddressFiltered.split("/");
		
		if( parts.length == 0 )
		{
			m_cachedParseError = bhE_CellAddressParseError.EMPTY;
			
			return;
		}
		else if( parts.length > bhS_App.MAX_CELL_ADDRESS_PARTS )
		{
			m_cachedParseError = bhE_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		for( int i = 0; i < parts.length; i++ )
		{
			String part = parts[i];
			
			bhE_SignUpValidationError error = bhI_SignUpCredentialValidator.USERNAME_VALIDATOR.validateCredential(part);
			
			if( error.isError() )
			{
				m_cachedParseError = bhE_CellAddressParseError.BAD_FORMAT;
				
				return;
			}
			
			if( m_rawAddress == null )
			{
				m_rawAddress = "/" + part;
				m_rawAddressNoLeadSlash = part;
			}
			else
			{
				m_rawAddress += "/" + part;
				m_rawAddressNoLeadSlash += "/" + part;
			}
			
			m_parts[i] = parts[i];
		}

		m_cachedParseError = bhE_CellAddressParseError.NO_ERROR;
	}
	
	public boolean isEqualTo(bhCellAddress address)
	{
		return this.m_rawAddress.equals(address.m_rawAddress);
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhJsonHelper.getInstance().putString(json, bhE_JsonKey.rawCellAddress, m_rawAddress);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		this.init(bhJsonHelper.getInstance().getString(json, bhE_JsonKey.rawCellAddress));
	}
	
	@Override
	public boolean isEqualTo(bhI_JsonObject json)
	{
		String rawAddress = bhJsonHelper.getInstance().getString(json, bhE_JsonKey.rawCellAddress);
		
		if( rawAddress != null )
		{
			if( rawAddress.equals(m_rawAddress) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return m_rawAddress;
	}
}