package b33hive.shared.entities;

public enum bhE_CharacterQuota
{
	FREE(4096),
	TIER_1(8192),
	TIER_2(8192*2),
	UNLIMITED(0);
	
	
	private final int m_maxCharacters;
	
	private bhE_CharacterQuota(int maxCharacters)
	{
		m_maxCharacters = maxCharacters;
	}
	
	public int getMaxCharacters()
	{
		return m_maxCharacters;
	}
}
