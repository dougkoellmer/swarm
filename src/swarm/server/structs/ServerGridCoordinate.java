package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.GridCoordinate;

/**
 * Externalizable can't be used on the client, so we must needs create a server version of smPoint just to read/write byte streams.
 * 
 * @author Doug
 *
 */
public class ServerGridCoordinate extends GridCoordinate implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public ServerGridCoordinate()
	{	
	}
	
	public ServerGridCoordinate(A_JsonFactory jsonFactory, I_JsonObject json)
	{	
		super(jsonFactory, json);
	}
	
	public ServerGridCoordinate(int m, int n)
	{
		super(m, n);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.getM());
		out.writeInt(this.getN());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int version = in.readInt();
		
		this.setM(in.readInt());
		this.setN(in.readInt());
	}
}
