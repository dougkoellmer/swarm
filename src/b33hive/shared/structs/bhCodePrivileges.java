package b33hive.shared.structs;


import b33hive.shared.app.bh;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CharacterQuota;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhCodePrivileges extends bhA_JsonEncodable
{
	protected bhE_NetworkPrivilege m_network;
	protected bhE_CharacterQuota m_characterQuota;
	
	public bhCodePrivileges()
	{
		setToDefault();
	}
	
	private void setToDefault()
	{
		m_network = bhE_NetworkPrivilege.IMAGES;
		m_characterQuota = bhE_CharacterQuota.FREE;
	}
	
	public bhCodePrivileges(bhI_JsonObject json)
	{
		super(json);
	}
	
	public void copy(bhCodePrivileges privileges)
	{
		if( privileges == null )
		{
			setToDefault();
			
			bhU_Debug.ASSERT(false, "Expected privileges to be not null in copy.");
			
			return;
		}
		
		m_network = privileges.m_network;
		m_characterQuota = privileges.m_characterQuota;
	}
	
	public bhE_NetworkPrivilege getNetworkPrivilege()
	{
		return m_network;
	}
	
	public bhE_CharacterQuota getCharacterQuota()
	{
		return m_characterQuota;
	}
	
	public static boolean isReadable(bhI_JsonObject json)
	{
		return bh.jsonFactory.getHelper().containsAnyKeys(json, bhE_JsonKey.networkPrivilege, bhE_JsonKey.characterQuota);
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bh.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.networkPrivilege, m_network);
		bh.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.characterQuota, m_characterQuota);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		bhE_NetworkPrivilege network = bh.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.networkPrivilege, bhE_NetworkPrivilege.values());
		m_network = network != null ? network : bhE_NetworkPrivilege.IMAGES;
		
		bhE_CharacterQuota quota = bh.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.characterQuota, bhE_CharacterQuota.values());
		m_characterQuota = quota == null ? bhE_CharacterQuota.FREE : quota;
	}
}
