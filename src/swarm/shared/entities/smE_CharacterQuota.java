package swarm.shared.entities;

public enum smE_CharacterQuota
{
	FREE(4096),
	TIER_1(8192),
	TIER_2(8192*2),
	UNLIMITED(0);
	
	
	private final int m_maxCharacters;
	
	private smE_CharacterQuota(int maxCharacters)
	{
		m_maxCharacters = maxCharacters;
	}
	
	public int getMaxCharacters()
	{
		return m_maxCharacters;
	}
}
