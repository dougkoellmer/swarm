package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smU_Blob;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smGridCoordinate;

/**
 * Externalizable can't be used on the client, so we must needs create a server version of bhPoint just to read/write byte streams.
 * 
 * @author Doug
 *
 */
public class smServerGridCoordinate extends bhGridCoordinate implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public smServerGridCoordinate()
	{	
	}
	
	public smServerGridCoordinate(int m, int n)
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
