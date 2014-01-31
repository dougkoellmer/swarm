package swarm.shared.structs;


import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CharacterQuota;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class CodePrivileges extends A_JsonEncodable
{
	protected E_NetworkPrivilege m_network;
	protected E_CharacterQuota m_characterQuota;
	
	public CodePrivileges()
	{
		setToDefault();
	}
	
	private void setToDefault()
	{
		m_network = E_NetworkPrivilege.IMAGES;
		m_characterQuota = E_CharacterQuota.FREE;
	}
	
	public CodePrivileges(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public void copy(CodePrivileges privileges)
	{
		if( privileges == null )
		{
			setToDefault();
			
			U_Debug.ASSERT(false, "Expected privileges to be not null in copy.");
			
			return;
		}
		
		m_network = privileges.m_network;
		m_characterQuota = privileges.m_characterQuota;
	}
	
	public E_NetworkPrivilege getNetworkPrivilege()
	{
		return m_network;
	}
	
	public E_CharacterQuota getCharacterQuota()
	{
		return m_characterQuota;
	}
	
	public void setCharacterQuota(E_CharacterQuota quota)
	{
		m_characterQuota = quota;
	}
	
	public static boolean isReadable(A_JsonFactory factory, I_JsonObject json)
	{
		return factory.getHelper().containsAnyKeys(json, E_JsonKey.networkPrivilege, E_JsonKey.characterQuota);
	}

	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putEnum(json_out, E_JsonKey.networkPrivilege, m_network);
		factory.getHelper().putEnum(json_out, E_JsonKey.characterQuota, m_characterQuota);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		E_NetworkPrivilege network = factory.getHelper().getEnum(json, E_JsonKey.networkPrivilege, E_NetworkPrivilege.values());
		m_network = network != null ? network : E_NetworkPrivilege.IMAGES;
		
		E_CharacterQuota quota = factory.getHelper().getEnum(json, E_JsonKey.characterQuota, E_CharacterQuota.values());
		m_characterQuota = quota == null ? E_CharacterQuota.FREE : quota;
	}
}
