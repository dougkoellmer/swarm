package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.CellSize;

public class ServerCellSize extends CellSize implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public ServerCellSize(I_JsonObject json, A_JsonFactory jsonFactory)
	{
		super(json, jsonFactory);
	}
	
	public ServerCellSize()
	{
		super();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		m_width = in.readInt();
		m_height = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		out.writeInt(m_width);
		out.writeInt(m_height);
	}
}
