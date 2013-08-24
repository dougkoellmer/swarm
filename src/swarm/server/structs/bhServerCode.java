package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.bhU_Serialization;
import swarm.shared.entities.bhE_CodeSafetyLevel;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.structs.bhCode;

public class bhServerCode extends bhCode implements Externalizable
{
	private static final int EXTERNAL_VERSION = 2;
	
	/**
	 * Constructor only for use by externalizable.
	 */
	public bhServerCode()
	{
		//--- DRK > Super will complain if we don't provide stand-in types,
		//---		so we give it all of them.  They'll be overwritten anyway by readExternal().
		super((String)null, bhE_CodeType.values());
	}
	
	public bhServerCode(bhI_JsonObject json, bhE_CodeType ... standInTypes)
	{
		super(json, standInTypes);
	}
	
	public bhServerCode(String rawCode, bhE_CodeType ... standInTypes)
	{
		super(rawCode, standInTypes);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.m_standInFlags);
		bhU_Serialization.writeNullableString(m_rawCode, out);
		bhU_Serialization.writeNullableEnum(m_safetyLevel, out);
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
			String[] codeParts = bhU_Serialization.readJavaArray(String.class, in);
			if( codeParts != null && codeParts.length > 0 )
			{
				m_rawCode = codeParts[0];
			}
		}
		else
		{
			m_rawCode = bhU_Serialization.readNullableString(in);
		}
		
		m_safetyLevel = bhU_Serialization.readNullableEnum(bhE_CodeSafetyLevel.values(), in);
	}
}
