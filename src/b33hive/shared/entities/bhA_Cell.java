package b33hive.shared.entities;

import b33hive.server.structs.bhServerCode;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonKeySource;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;

public abstract class bhA_Cell extends bhA_JsonEncodable
{
	private final bhGridCoordinate m_coordinate;

	protected final bhCode m_code[] = new bhCode[bhE_CodeType.values().length];
	
	protected bhCodePrivileges m_codePrivileges;
	
	protected bhA_Cell()
	{
		m_coordinate = new bhGridCoordinate();
		
		m_codePrivileges = new bhCodePrivileges();
	}
	
	public bhA_Cell(bhCodePrivileges privileges)
	{
		m_coordinate = new bhGridCoordinate();
		
		m_codePrivileges = privileges;
	}
	
	public bhA_Cell(bhGridCoordinate coordinate)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = new bhCodePrivileges();
	}
	
	public bhA_Cell(bhGridCoordinate coordinate, bhCodePrivileges privileges)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = privileges;
	}
	
	public bhGridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	public bhCodePrivileges getCodePrivileges()
	{
		return m_codePrivileges;
	}
	
	protected void clearCode()
	{
		for( int i = 0; i < m_code.length; i++ )
		{
			m_code[i] = null;
		}
	}
	
	public void copy(bhA_Cell from)
	{
		for( int i = 0; i < m_code.length; i++ )
		{
			bhE_CodeType type = bhE_CodeType.values()[i];
			if( from.m_code[i] != null )
			{
				this.setCode(type, from.m_code[i]);
				
				if( type == bhE_CodeType.SOURCE || from.m_code[i].getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
				{
					if( from.getCodePrivileges() != null && this.getCodePrivileges() != null )
					{
						this.getCodePrivileges().copy(from.getCodePrivileges());
					}
				}
			}
		}
	}
	
	public void setCode(bhE_CodeType eType, bhCode code_nullable)
	{
		m_code[eType.ordinal()] = code_nullable;
	}
	
	public bhCode getStandInCode(bhE_CodeType eType)
	{
		bhCode code = getCode(eType);
		
		if( code == null )
		{
			for( int i = 0; i < m_code.length; i++ )
			{
				if( i == eType.ordinal() )  continue;
				
				code = getCode(bhE_CodeType.values()[i]);
				
				if( code != null )
				{
					if( code.isStandInFor(eType) )
					{
						return code;
					}
				}
			}
			
			return null;
		}
		else
		{
			return code;
		}
	}
	
	public bhCode getCode(bhE_CodeType eType)
	{
		return m_code[eType.ordinal()];
	}
	
	/*@Override
	public boolean isEqualTo(bhI_JsonObject json)
	{
		for( int i = 0; i < bhE_CodeType.values().length; i++ )
		{
			bhI_JsonKeySource key = bhE_CodeType.values()[i].getJsonKey();
			bhI_JsonObject jsonForCode = bhU_Json.getJsonObject(json, key);
			
			if( m_code[i] == null && jsonForCode == null )
			{
				continue;
			}
			else if( m_code[i] != null && jsonForCode != null )
			{
				bhU_Json.putJsonObject(json, bhE_CodeType.values()[i].getJsonKey(), m_code[i].writeJson());
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}*/
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_codePrivileges != null )
		{
			m_codePrivileges.writeJson(json);
		}
		
		for( int i = 0; i < bhE_CodeType.values().length; i++ )
		{
			if( m_code[i] != null )
			{
				bhJsonHelper.getInstance().putJsonObject(json, bhE_CodeType.values()[i].getJsonKey(), m_code[i].writeJson());
			}
		}
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		if( bhCodePrivileges.isReadable(json) )
		{
			if( m_codePrivileges == null )
			{
				m_codePrivileges = new bhCodePrivileges();
			}
			
			m_codePrivileges.readJson(json);
		}
		else
		{
			m_codePrivileges = null;
		}
		
		for( int i = 0; i < bhE_CodeType.values().length; i++ )
		{
			bhI_JsonKeySource key = bhE_CodeType.values()[i].getJsonKey();
			
			bhI_JsonObject jsonForCode = bhJsonHelper.getInstance().getJsonObject(json, key);

			if( jsonForCode != null )
			{
				m_code[i] = new bhCode(jsonForCode, bhE_CodeType.values()[i]);
			}
			else
			{
				m_code[i] = null;
			}
		}
		
		//--- DRK > This loop fills in any blanks using standins, if they're available.
		for( int i = 0; i < bhE_CodeType.values().length; i++ )
		{
			if( m_code[i] == null )
			{
				for( int j = 0; j < bhE_CodeType.values().length; j++ )
				{
					if( i == j ) continue;
					
					if( m_code[j] != null && m_code[j].isStandInFor(bhE_CodeType.values()[i]) )
					{
						m_code[i] = m_code[j];
					}
				}
			}
		}
	}
}
