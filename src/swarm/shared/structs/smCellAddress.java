package swarm.shared.structs;

import java.util.ArrayList;

import swarm.shared.app.sm;
import swarm.shared.app.smS_App;
import swarm.shared.account.smE_SignUpValidationError;
import swarm.shared.account.smI_SignUpCredentialValidator;
import swarm.shared.account.smS_Account;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.utils.smU_Regex;

public class smCellAddress extends smA_JsonEncodable
{
	public static enum E_Part
	{
		USERNAME,
		CELL
	}
	
	private String m_rawAddress = null;
	private String m_caseSensitiveRawAddress = null;
	
	private final String[] m_parts = new String[E_Part.values().length];
	
	private smE_CellAddressParseError m_parseError = null;
	
	public smCellAddress()
	{
		init(null);
	}
	
	public smCellAddress(String rawAddress)
	{
		init(rawAddress);
	}
	
	public smCellAddress(smI_JsonObject json)
	{
		init(null);
		
		this.readJson(json);
	}
	
	public smCellAddress(smCellAddress source)
	{
		this(source.getRawAddressLeadSlash());
	}
	
	public boolean isValid()
	{
		return this.getParseError() == smE_CellAddressParseError.NO_ERROR;
	}
	
	protected void init(String rawAddress)
	{
		m_caseSensitiveRawAddress = null;
		m_rawAddress = null;
		m_parseError = null;
		for( int i = 0; i < m_parts.length; i++ )
		{
			m_parts[i] = null;
		}
		
		parse(rawAddress);
	}
	
	public smE_CellAddressParseError getParseError()
	{
		return m_parseError;
	}
	
	public String getRawAddressLeadSlash()
	{
		return "/" + m_rawAddress;
	}
	
	public String getRawAddress()
	{
		return m_rawAddress;
	}
	
	public String getCasedRawAddress()
	{
		return m_caseSensitiveRawAddress;
	}
	
	public String getCasedRawAddressLeadSlash()
	{
		return "/" + m_caseSensitiveRawAddress;
	}
	
	public String getPart(E_Part part)
	{
		return m_parts[part.ordinal()];
	}
	
	private void parse(String rawAddress)
	{
		m_caseSensitiveRawAddress = null;
		m_rawAddress = null;
		
		if( rawAddress == null || rawAddress.length() == 0 )
		{
			m_parseError = smE_CellAddressParseError.EMPTY;
			
			return;
		}
		
		//--- DRK > For some reason, the split function below won't recognize multiple trailing slashes, only leading slashes.
		//---		So we just account for that strange case simply here.
		if( rawAddress.contains("//") )
		{
			m_parseError = smE_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		//--- DRK > Strip off leading and trailing slashes.
		String rawAddressFiltered = rawAddress.startsWith("/") ? rawAddress.replaceFirst("/", "") : rawAddress;
		rawAddressFiltered = rawAddressFiltered.endsWith("/") ? rawAddressFiltered.substring(0, rawAddressFiltered.length()-1) : rawAddressFiltered;
		
		if( rawAddressFiltered == null || rawAddressFiltered.length() == 0 )
		{
			m_parseError = smE_CellAddressParseError.EMPTY;
			
			return;
		}
		
		String[] parts = rawAddressFiltered.split("/");
		
		if( parts.length == 0 )
		{
			m_parseError = smE_CellAddressParseError.EMPTY;
			
			return;
		}
		else if( parts.length > smS_App.MAX_CELL_ADDRESS_PARTS )
		{
			m_parseError = smE_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		for( int i = 0; i < parts.length; i++ )
		{
			String part = parts[i];
			
			if( !bhU_Regex.calcIsMatch(part, smS_Account.USERNAME_REGEX) )
			{
				m_parseError = smE_CellAddressParseError.BAD_FORMAT;
				
				return;
			}
			
			String partToLowerCase = part.toLowerCase();
			
			if( m_rawAddress == null )
			{
				m_rawAddress = partToLowerCase;
				m_caseSensitiveRawAddress = part;
			}
			else
			{
				m_rawAddress += "/" + partToLowerCase;
				m_caseSensitiveRawAddress += "/" + part;
			}
			
			m_parts[i] = partToLowerCase;
		}

		m_parseError = smE_CellAddressParseError.NO_ERROR;
	}
	
	public boolean isEqualTo(smCellAddress address)
	{
		return this.m_rawAddress.equals(address.m_rawAddress);
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, smE_JsonKey.rawCellAddress, getCasedRawAddress());
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		this.init(sm.jsonFactory.getHelper().getString(json, smE_JsonKey.rawCellAddress));
	}
	
	@Override
	public boolean isEqualTo(smI_JsonObject json)
	{
		String rawAddress = sm.jsonFactory.getHelper().getString(json, smE_JsonKey.rawCellAddress);
		
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