package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.U_Serialization;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.Code;

public class ServerCode extends Code implements Externalizable
{
	private static final int EXTERNAL_VERSION = 2;
	
	/**
	 * Constructor only for use by externalizable.
	 */
	public ServerCode()
	{
		//--- DRK > Super will complain if we don't provide stand-in types,
		//---		so we give it all of them.  They'll be overwritten anyway by readExternal().
		super((String)null, E_CodeType.values());
	}
	
	public ServerCode(A_JsonFactory jsonFactory, I_JsonObject json, E_CodeType ... standInTypes)
	{
		super(jsonFactory, json, standInTypes);
	}
	
	public ServerCode(String rawCode, E_CodeType ... standInTypes)
	{
		super(rawCode, standInTypes);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.m_standInFlags);
		U_Serialization.writeNullableString(m_rawCode, out);
		U_Serialization.writeNullableEnum(m_safetyLevel, out);
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
			String[] codeParts = U_Serialization.readJavaArray(String.class, in);
			if( codeParts != null && codeParts.length > 0 )
			{
				m_rawCode = codeParts[0];
			}
		}
		else
		{
			m_rawCode = U_Serialization.readNullableString(in);
		}
		
		m_safetyLevel = U_Serialization.readNullableEnum(E_CodeSafetyLevel.values(), in);
	}
}
