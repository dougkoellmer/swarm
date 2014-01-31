package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.U_Serialization;
import swarm.shared.entities.E_CharacterQuota;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_NetworkPrivilege;

public class ServerCodePrivileges extends CodePrivileges implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public void setNetworkPrivilege(E_NetworkPrivilege network)
	{
		m_network = network;
	}
	
	public void setCharacterQuota(E_CharacterQuota quota)
	{
		m_characterQuota = quota;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		U_Serialization.writeNullableEnum(m_network, out);
		U_Serialization.writeNullableEnum(m_characterQuota, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_network = U_Serialization.readNullableEnum(E_NetworkPrivilege.values(), in);
		m_characterQuota = U_Serialization.readNullableEnum(E_CharacterQuota.values(), in);
	}
}
