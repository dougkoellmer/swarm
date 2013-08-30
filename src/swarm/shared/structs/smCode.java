package swarm.shared.structs;

import swarm.shared.utils.smU_BitTricks;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCode extends smA_JsonEncodable
{
	protected String m_rawCode;
	
	protected int m_standInFlags;
	
	protected smE_CodeSafetyLevel m_safetyLevel;
	
	public smCode(smI_JsonObject json, smE_CodeType ... standInTypes)
	{
		super(json);

		init(standInTypes);
	}
	
	public smCode(String rawCode, smE_CodeType ... standInTypes)
	{
		m_rawCode = rawCode;
		
		init(standInTypes);
	}
	
	public void setSafetyLevel(smE_CodeSafetyLevel safetyLevel)
	{
		m_safetyLevel = safetyLevel;
	}
	
	public smE_CodeSafetyLevel getSafetyLevel()
	{
		return m_safetyLevel;
	}
	
	private void initWithStandInType(smE_CodeType standInType)
	{
		m_standInFlags |= smU_BitTricks.calcOrdinalBit(standInType.ordinal());
	}
	
	private void init(smE_CodeType[] standInTypes)
	{
		m_safetyLevel = m_safetyLevel == null? smE_CodeSafetyLevel.SAFE : m_safetyLevel;
		
		if( standInTypes.length == 0 )
		{
			throw new RuntimeException("Code must have at least one stand-in type assigned.");
		}
		
		for( int i = 0; i < standInTypes.length; i++ )
		{
			initWithStandInType(standInTypes[i]);
		}
	}
	
	public String getRawCode()
	{
		return m_rawCode;
	}
	
	public boolean isStandInFor(smE_CodeType type)
	{
		if( (smU_BitTricks.calcOrdinalBit(type.ordinal()) & m_standInFlags) != 0 )
		{
			return true;
		}
		
		return false;
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
		if( isEmpty() )
		{
			return 0;
		}
		
		return m_rawCode.length();
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, smE_JsonKey.rawCode, m_rawCode);
		factory.getHelper().putInt(json_out, smE_JsonKey.standInFlags, m_standInFlags);
		factory.getHelper().putEnum(json_out, smE_JsonKey.codeSafetyLevel, m_safetyLevel);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_standInFlags = 0x0;
		
		m_rawCode = factory.getHelper().getString(json, smE_JsonKey.rawCode);
		
		m_standInFlags = factory.getHelper().getInt(json, smE_JsonKey.standInFlags);
		
		m_safetyLevel = factory.getHelper().getEnum(json, smE_JsonKey.codeSafetyLevel, smE_CodeSafetyLevel.values());
	}
}
