package swarm.shared.structs;

import swarm.shared.utils.U_Bits;
import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class Code extends A_JsonEncodable
{
	protected String m_rawCode;
	
	protected int m_standInFlags;
	
	protected E_CodeSafetyLevel m_safetyLevel;
	
	public Code(A_JsonFactory jsonFactory, I_JsonObject json, E_CodeType ... standInTypes)
	{
		super(jsonFactory, json);

		init(standInTypes);
	}
	
	public Code(String rawCode, E_CodeType ... standInTypes)
	{
		m_rawCode = rawCode;
		
		init(standInTypes);
	}
	
	public void setSafetyLevel(E_CodeSafetyLevel safetyLevel)
	{
		m_safetyLevel = safetyLevel;
	}
	
	public E_CodeSafetyLevel getSafetyLevel()
	{
		return m_safetyLevel;
	}
	
	private void addStandInType(E_CodeType standInType)
	{
		m_standInFlags |= U_Bits.calcOrdinalBit(standInType.ordinal());
	}
	
	private void init(E_CodeType[] standInTypes)
	{
		m_safetyLevel = m_safetyLevel == null? E_CodeSafetyLevel.NO_SANDBOX_STATIC : m_safetyLevel;
		
		if( standInTypes.length == 0 )
		{
			throw new RuntimeException("Code must have at least one stand-in type assigned.");
		}
		
		for( int i = 0; i < standInTypes.length; i++ )
		{
			addStandInType(standInTypes[i]);
		}
	}
	
	public String getRawCode()
	{
		return m_rawCode;
	}
	
	public boolean isStandInFor(E_CodeType type)
	{
		return (U_Bits.calcOrdinalBit(type.ordinal()) & m_standInFlags) != 0;
	}
	
	public int getStandInFlags()
	{
		return m_standInFlags;
	}
	
	public boolean isEmpty()
	{
		return m_rawCode == null || m_rawCode.length() == 0;
	}
	
	public int getRawCodeLength()
	{
		return isEmpty() ? 0 : m_rawCode.length();
	}

	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		factory.getHelper().putString(json_out, E_JsonKey.rawCode, m_rawCode);
		factory.getHelper().putInt(json_out, E_JsonKey.standInFlags, m_standInFlags);
		factory.getHelper().putEnum(json_out, E_JsonKey.codeSafetyLevel, m_safetyLevel);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_standInFlags = 0x0;
		
		m_rawCode = factory.getHelper().getString(json, E_JsonKey.rawCode);
		
		m_standInFlags = factory.getHelper().getInt(json, E_JsonKey.standInFlags);
		
		m_safetyLevel = factory.getHelper().getEnum(json, E_JsonKey.codeSafetyLevel, E_CodeSafetyLevel.values());
	}
}
