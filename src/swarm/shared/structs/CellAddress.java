package swarm.shared.structs;

import java.util.ArrayList;

import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.I_SignUpCredentialValidator;
import swarm.shared.account.S_Account;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonComparable;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.utils.U_Regex;

public class CellAddress extends A_JsonEncodable implements I_JsonComparable
{
	public static enum E_Part
	{
		USERNAME,
		CELL
	}
	
	private String m_rawAddress = null;
	private String m_caseSensitiveRawAddress = null;
	
	private final ArrayList<String> m_parts = new ArrayList<String>();
	
	private E_CellAddressParseError m_parseError = null;
	
	public CellAddress()
	{
		init(null);
	}
	
	public CellAddress(String rawAddress)
	{
		init(rawAddress);
	}
	
	public CellAddress(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		init(null);
		
		this.readJson(jsonFactory, json);
	}
	
	public CellAddress(CellAddress source)
	{
		this(source.getRawAddressLeadSlash());
	}
	
	public boolean isValid()
	{
		return this.getParseError() == E_CellAddressParseError.NO_ERROR;
	}
	
	protected void init(String rawAddress)
	{
		m_caseSensitiveRawAddress = null;
		m_rawAddress = null;
		m_parseError = null;
		m_parts.clear();
		
		parse(rawAddress);
	}
	
	public E_CellAddressParseError getParseError()
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
		return part.ordinal() >= m_parts.size() ? null : m_parts.get(part.ordinal());
	}
	
	private void parse(String rawAddress)
	{
		m_caseSensitiveRawAddress = null;
		m_rawAddress = null;
		
		if( rawAddress == null || rawAddress.length() == 0 )
		{
			m_parseError = E_CellAddressParseError.EMPTY;
			
			return;
		}
		
		//--- DRK > For some reason, the split function below won't recognize multiple trailing slashes, only leading slashes.
		//---		So we just account for that strange case simply here.
		if( rawAddress.contains("//") )
		{
			m_parseError = E_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		//--- DRK > Strip off leading and trailing slashes.
		String rawAddressFiltered = rawAddress.startsWith("/") ? rawAddress.replaceFirst("/", "") : rawAddress;
		rawAddressFiltered = rawAddressFiltered.endsWith("/") ? rawAddressFiltered.substring(0, rawAddressFiltered.length()-1) : rawAddressFiltered;
		
		if( rawAddressFiltered == null || rawAddressFiltered.length() == 0 )
		{
			m_parseError = E_CellAddressParseError.EMPTY;
			
			return;
		}
		
		String[] parts = rawAddressFiltered.split("/");
		
		if( parts.length == 0 )
		{
			m_parseError = E_CellAddressParseError.EMPTY;
			
			return;
		}
		else if( parts.length > S_CommonApp.MAX_CELL_ADDRESS_PARTS )
		{
			m_parseError = E_CellAddressParseError.TOO_MANY_PARTS;
			
			return;
		}
		
		for( int i = 0; i < parts.length; i++ )
		{
			String part = parts[i];
			
			if( !U_Regex.calcIsMatch(part, S_Account.USERNAME_REGEX) )
			{
				m_parseError = E_CellAddressParseError.BAD_FORMAT;
				
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
			
			m_parts.add(partToLowerCase);
		}

		m_parseError = E_CellAddressParseError.NO_ERROR;
	}
	
	public boolean isEqualTo(CellAddress address)
	{
		return this.m_rawAddress.equals(address.m_rawAddress);
	}

	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, E_JsonKey.rawCellAddress, getCasedRawAddress());
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		this.init(factory.getHelper().getString(json, E_JsonKey.rawCellAddress));
	}
	
	@Override
	public boolean isEqualTo(A_JsonFactory factory, I_JsonObject json)
	{
		String rawAddress = factory.getHelper().getString(json, E_JsonKey.rawCellAddress);
		
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