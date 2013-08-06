package b33hive.shared.structs;

import b33hive.shared.utils.bhU_BitTricks;
import b33hive.shared.app.bh;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

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
		bh.jsonFactory.getHelper().putString(json, bhE_JsonKey.rawCode, m_rawCode);
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.standInFlags, m_standInFlags);
		bh.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.codeSafetyLevel, m_safetyLevel);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_standInFlags = 0x0;
		
		m_rawCode = bh.jsonFactory.getHelper().getString(json, bhE_JsonKey.rawCode);
		
		m_standInFlags = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.standInFlags);
		
		m_safetyLevel = bh.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.codeSafetyLevel, bhE_CodeSafetyLevel.values());
	}
}
