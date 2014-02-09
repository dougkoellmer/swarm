package swarm.shared.entities;

import swarm.server.structs.ServerCode;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonKeySource;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.CellSize;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;

public abstract class A_Cell extends A_JsonEncodable
{
	protected final int DEFAULT_DIMENSION = -1;
	
	private final GridCoordinate m_coordinate;

	protected final Code m_code[] = new Code[E_CodeType.values().length];
	
	protected CodePrivileges m_codePrivileges;
	
	protected final CellSize m_focusedCellSize = newCellSize();
	
	protected A_Cell()
	{
		m_coordinate = new GridCoordinate();
		
		m_codePrivileges = new CodePrivileges();
	}
	
	public A_Cell(CodePrivileges privileges)
	{
		m_coordinate = new GridCoordinate();
		
		m_codePrivileges = privileges;
	}
	
	public A_Cell(GridCoordinate coordinate)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = new CodePrivileges();
	}
	
	public A_Cell(GridCoordinate coordinate, CodePrivileges privileges)
	{
		m_coordinate = coordinate;
		
		m_codePrivileges = privileges;
	}
	
	public CellSize getFocusedCellSize()
	{
		return m_focusedCellSize;
	}
	
	public GridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	public CodePrivileges getCodePrivileges()
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
	
	public void copy(A_Cell from)
	{
		for( int i = 0; i < m_code.length; i++ )
		{
			E_CodeType type = E_CodeType.values()[i];
			if( from.m_code[i] != null )
			{
				this.setCode(type, from.m_code[i]);
				
				if( type == E_CodeType.SOURCE || from.m_code[i].getSafetyLevel() == E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX )
				{
					if( from.getCodePrivileges() != null && this.getCodePrivileges() != null )
					{
						this.getCodePrivileges().copy(from.getCodePrivileges());
					}
				}
			}
		}
	}
	
	public void setCode(E_CodeType eType, Code code_nullable)
	{
		m_code[eType.ordinal()] = code_nullable;
	}
	
	public Code getStandInCode(E_CodeType eType)
	{
		Code code = getCode(eType);
		
		if( code == null )
		{
			for( int i = 0; i < m_code.length; i++ )
			{
				if( i == eType.ordinal() )  continue;
				
				code = getCode(E_CodeType.values()[i]);
				
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
	
	protected CellSize newCellSize()
	{
		return new CellSize();
	}
	
	public Code getCode(E_CodeType eType)
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
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		if( m_codePrivileges != null )
		{
			m_codePrivileges.writeJson(json_out, factory);
		}
		
		for( int i = 0; i < E_CodeType.values().length; i++ )
		{
			if( m_code[i] != null )
			{
				I_JsonObject codeJson = factory.createJsonObject();
				m_code[i].writeJson(codeJson, factory);
				factory.getHelper().putJsonObject(json_out, E_CodeType.values()[i].getJsonKey(), codeJson);
			}
		}
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		if( CodePrivileges.isReadable(factory, json) )
		{
			if( m_codePrivileges == null )
			{
				m_codePrivileges = new CodePrivileges();
			}
			
			m_codePrivileges.readJson(json, factory);
		}
		else
		{
			m_codePrivileges = null;
		}
		
		for( int i = 0; i < E_CodeType.values().length; i++ )
		{
			I_JsonKeySource key = E_CodeType.values()[i].getJsonKey();
			
			I_JsonObject jsonForCode = factory.getHelper().getJsonObject(json, key);

			if( jsonForCode != null )
			{
				m_code[i] = new Code(factory, jsonForCode, E_CodeType.values()[i]);
			}
			else
			{
				m_code[i] = null;
			}
		}
		
		//--- DRK > This loop fills in any blanks using standins, if they're available.
		for( int i = 0; i < E_CodeType.values().length; i++ )
		{
			if( m_code[i] == null )
			{
				for( int j = 0; j < E_CodeType.values().length; j++ )
				{
					if( i == j ) continue;
					
					if( m_code[j] != null && m_code[j].isStandInFor(E_CodeType.values()[i]) )
					{
						m_code[i] = m_code[j];
					}
				}
			}
		}
	}
}
