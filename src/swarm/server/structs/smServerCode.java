package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.smU_Serialization;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smCode;

public class smServerCode extends smCode implements Externalizable
{
	private static final int EXTERNAL_VERSION = 2;
	
	/**
	 * Constructor only for use by externalizable.
	 */
	public smServerCode()
	{
		//--- DRK > Super will complain if we don't provide stand-in types,
		//---		so we give it all of them.  They'll be overwritten anyway by readExternal().
		super((String)null, smE_CodeType.values());
	}
	
	public smServerCode(smI_JsonObject json, smE_CodeType ... standInTypes)
	{
		super(json, standInTypes);
	}
	
	public smServerCode(String rawCode, smE_CodeType ... standInTypes)
	{
		super(rawCode, standInTypes);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.m_standInFlags);
		smU_Serialization.writeNullableString(m_rawCode, out);
		smU_Serialization.writeNullableEnum(m_safetyLevel, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_standInFlags = in.readInt();
		if( externalVersion == 1 )
		{
			//--- DRK > Should do an admin bulk recompile of cells, which should be the only time this case is hit.
			//---		Can remove sometime in future.
			String[] codeParts = smU_Serialization.readJavaArray(String.class, in);
			if( codeParts != null && codeParts.length > 0 )
			{
				m_rawCode = codeParts[0];
			}
		}
		else
		{
			m_rawCode = smU_Serialization.readNullableString(in);
		}
		
		m_safetyLevel = smU_Serialization.readNullableEnum(smE_CodeSafetyLevel.values(), in);
	}
}
