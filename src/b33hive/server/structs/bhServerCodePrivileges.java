package com.b33hive.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.b33hive.server.data.blob.bhU_Serialization;
import com.b33hive.shared.entities.bhE_CharacterQuota;
import com.b33hive.shared.structs.bhCodePrivileges;
import com.b33hive.shared.structs.bhE_NetworkPrivilege;

public class bhServerCodePrivileges extends bhCodePrivileges implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public void setNetworkPrivilege(bhE_NetworkPrivilege network)
	{
		m_network = network;
	}
	
	public void setCharacterQuota(bhE_CharacterQuota quota)
	{
		m_characterQuota = quota;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		bhU_Serialization.writeNullableEnum(m_network, out);
		bhU_Serialization.writeNullableEnum(m_characterQuota, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_network = bhU_Serialization.readNullableEnum(bhE_NetworkPrivilege.values(), in);
		m_characterQuota = bhU_Serialization.readNullableEnum(bhE_CharacterQuota.values(), in);
	}
}
