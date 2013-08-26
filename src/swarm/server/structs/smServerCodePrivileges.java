package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.smU_Serialization;
import swarm.shared.entities.smE_CharacterQuota;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smE_NetworkPrivilege;

public class smServerCodePrivileges extends smCodePrivileges implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public void setNetworkPrivilege(smE_NetworkPrivilege network)
	{
		m_network = network;
	}
	
	public void setCharacterQuota(smE_CharacterQuota quota)
	{
		m_characterQuota = quota;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		smU_Serialization.writeNullableEnum(m_network, out);
		smU_Serialization.writeNullableEnum(m_characterQuota, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_network = smU_Serialization.readNullableEnum(smE_NetworkPrivilege.values(), in);
		m_characterQuota = smU_Serialization.readNullableEnum(smE_CharacterQuota.values(), in);
	}
}
