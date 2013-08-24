package swarm.shared.structs;

import swarm.shared.utils.bhU_BitTricks;
import swarm.shared.app.sm;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhE_CodeSafetyLevel;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public class bhCode extends bhA_JsonEncodable
{
	protected String m_rawCode;
	
	protected int m_standInFlags;
	
	protected bhE_CodeSafetyLevel m_safetyLevel;
	
	public bhCode(bhI_JsonObject json, bhE_CodeType ... standInTypes)
	{
		super(json);

		init(standInTypes);
	}
	
	public bhCode(String rawCode, bhE_CodeType ... standInTypes)
	{
		m_rawCode = rawCode;
		
		init(standInTypes);
	}
	
	public void setSafetyLevel(bhE_CodeSafetyLevel safetyLevel)
	{
		m_safetyLevel = safetyLevel;
	}
	
	public bhE_CodeSafetyLevel getSafetyLevel()
	{
		return m_safetyLevel;
	}
	
	private void initWithStandInType(bhE_CodeType standInType)
	{
		m_standInFlags |= bhU_BitTricks.calcOrdinalBit(standInType.ordinal());
	}
	
	private void init(bhE_CodeType[] standInTypes)
	{
		m_safetyLevel = m_safetyLevel == null? bhE_CodeSafetyLevel.SAFE : m_safetyLevel;
		
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
	
	public boolean isStandInFor(bhE_CodeType type)
	{
		if( (bhU_BitTricks.calcOrdinalBit(type.ordinal()) & m_standInFlags) != 0 )
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
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, bhE_JsonKey.rawCode, m_rawCode);
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.standInFlags, m_standInFlags);
		sm.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.codeSafetyLevel, m_safetyLevel);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_standInFlags = 0x0;
		
		m_rawCode = sm.jsonFactory.getHelper().getString(json, bhE_JsonKey.rawCode);
		
		m_standInFlags = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.standInFlags);
		
		m_safetyLevel = sm.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.codeSafetyLevel, bhE_CodeSafetyLevel.values());
	}
}
