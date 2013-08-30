package swarm.shared.entities;

import swarm.server.structs.smServerCode;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonKeySource;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;

public abstract class smA_Cell extends smA_JsonEncodable
{
	private final smGridCoordinate m_coordinate;

	protected final smCode m_code[] = new smCode[smE_CodeType.values().length];
	
	protected smCodePrivileges m_codePrivileges;
	
	protected smA_Cell()
	{
		m_coordinate = new smGridCoordinate();
		
		m_codePrivileges = new smCodePrivileges();
	}
	
	public smA_Cell(smCodePrivileges privileges)
	{
		m_coordinate = new smGridCoordinate();
		
		m_codePrivileges = privileges;
	}
	
	public smA_Cell(smGridCoordinate coordinate)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = new smCodePrivileges();
	}
	
	public smA_Cell(smGridCoordinate coordinate, smCodePrivileges privileges)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = privileges;
	}
	
	public smGridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	public smCodePrivileges getCodePrivileges()
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
	
	public void copy(smA_Cell from)
	{
		for( int i = 0; i < m_code.length; i++ )
		{
			smE_CodeType type = smE_CodeType.values()[i];
			if( from.m_code[i] != null )
			{
				this.setCode(type, from.m_code[i]);
				
				if( type == smE_CodeType.SOURCE || from.m_code[i].getSafetyLevel() == smE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
				{
					if( from.getCodePrivileges() != null && this.getCodePrivileges() != null )
					{
						this.getCodePrivileges().copy(from.getCodePrivileges());
					}
				}
			}
		}
	}
	
	public void setCode(smE_CodeType eType, smCode code_nullable)
	{
		m_code[eType.ordinal()] = code_nullable;
	}
	
	public smCode getStandInCode(smE_CodeType eType)
	{
		smCode code = getCode(eType);
		
		if( code == null )
		{
			for( int i = 0; i < m_code.length; i++ )
			{
				if( i == eType.ordinal() )  continue;
				
				code = getCode(smE_CodeType.values()[i]);
				
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
	
	public smCode getCode(smE_CodeType eType)
	{
		return m_code[eType.ordinal()];
	}
	
	/*@Override
	public boolean isEqualTo(smI_JsonObject json)
	{
		for( int i = 0; i < smE_CodeType.values().length; i++ )
		{
			smI_JsonKeySource key = smE_CodeType.values()[i].getJsonKey();
			smI_JsonObject jsonForCode = smU_Json.getJsonObject(json, key);
			
			if( m_code[i] == null && jsonForCode == null )
			{
				continue;
			}
			else if( m_code[i] != null && jsonForCode != null )
			{
				smU_Json.putJsonObject(json, smE_CodeType.values()[i].getJsonKey(), m_code[i].writeJson());
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}*/
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		if( m_codePrivileges != null )
		{
			m_codePrivileges.writeJson(null, json_out);
		}
		
		for( int i = 0; i < smE_CodeType.values().length; i++ )
		{
			if( m_code[i] != null )
			{
				factory.getHelper().putJsonObject(json_out, smE_CodeType.values()[i].getJsonKey(), m_code[i].writeJson(null));
			}
		}
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		if( smCodePrivileges.isReadable(factory, json) )
		{
			if( m_codePrivileges == null )
			{
				m_codePrivileges = new smCodePrivileges();
			}
			
			m_codePrivileges.readJson(null, json);
		}
		else
		{
			m_codePrivileges = null;
		}
		
		for( int i = 0; i < smE_CodeType.values().length; i++ )
		{
			smI_JsonKeySource key = smE_CodeType.values()[i].getJsonKey();
			
			smI_JsonObject jsonForCode = factory.getHelper().getJsonObject(json, key);

			if( jsonForCode != null )
			{
				m_code[i] = new smCode(jsonForCode, smE_CodeType.values()[i]);
			}
			else
			{
				m_code[i] = null;
			}
		}
		
		//--- DRK > This loop fills in any blanks using standins, if they're available.
		for( int i = 0; i < smE_CodeType.values().length; i++ )
		{
			if( m_code[i] == null )
			{
				for( int j = 0; j < smE_CodeType.values().length; j++ )
				{
					if( i == j ) continue;
					
					if( m_code[j] != null && m_code[j].isStandInFor(smE_CodeType.values()[i]) )
					{
						m_code[i] = m_code[j];
					}
				}
			}
		}
	}
}
