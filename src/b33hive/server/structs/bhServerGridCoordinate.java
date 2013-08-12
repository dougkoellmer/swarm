package b33hive.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKey;
import b33hive.server.data.blob.bhU_Blob;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.structs.bhGridCoordinate;

/**
 * Externalizable can't be used on the client, so we must needs create a server version of bhPoint just to read/write byte streams.
 * 
 * @author Doug
 *
 */
public class bhServerGridCoordinate extends bhGridCoordinate implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	public bhServerGridCoordinate()
	{	
	}
	
	public bhServerGridCoordinate(int m, int n)
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
