package swarm.shared.structs;


import swarm.shared.app.smSharedAppContext;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CharacterQuota;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCodePrivileges extends smA_JsonEncodable
{
	protected smE_NetworkPrivilege m_network;
	protected smE_CharacterQuota m_characterQuota;
	
	public smCodePrivileges()
	{
		setToDefault();
	}
	
	private void setToDefault()
	{
		m_network = smE_NetworkPrivilege.IMAGES;
		m_characterQuota = smE_CharacterQuota.FREE;
	}
	
	public smCodePrivileges(smI_JsonObject json)
	{
		super(json);
	}
	
	public void copy(smCodePrivileges privileges)
	{
		if( privileges == null )
		{
			setToDefault();
			
			smU_Debug.ASSERT(false, "Expected privileges to be not null in copy.");
			
			return;
		}
		
		m_network = privileges.m_network;
		m_characterQuota = privileges.m_characterQuota;
	}
	
	public smE_NetworkPrivilege getNetworkPrivilege()
	{
		return m_network;
	}
	
	public smE_CharacterQuota getCharacterQuota()
	{
		return m_characterQuota;
	}
	
	public static boolean isReadable(smA_JsonFactory factory, smI_JsonObject json)
	{
		return factory.getHelper().containsAnyKeys(json, smE_JsonKey.networkPrivilege, smE_JsonKey.characterQuota);
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putEnum(json_out, smE_JsonKey.networkPrivilege, m_network);
		factory.getHelper().putEnum(json_out, smE_JsonKey.characterQuota, m_characterQuota);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		smE_NetworkPrivilege network = factory.getHelper().getEnum(json, smE_JsonKey.networkPrivilege, smE_NetworkPrivilege.values());
		m_network = network != null ? network : smE_NetworkPrivilege.IMAGES;
		
		smE_CharacterQuota quota = factory.getHelper().getEnum(json, smE_JsonKey.characterQuota, smE_CharacterQuota.values());
		m_characterQuota = quota == null ? smE_CharacterQuota.FREE : quota;
	}
}