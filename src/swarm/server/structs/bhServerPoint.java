package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.shared.structs.bhPoint;

/**
 * Externalizable can't be used on the client, so we must needs create a server version of bhPoint just to read/write byte streams.
 * @author Doug
 *
 */
public class bhServerPoint extends bhPoint implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeDouble(this.getX());
		out.writeDouble(this.getY());
		out.writeDouble(this.getZ());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int version = in.readInt();
		
		this.setX(in.readDouble());
		this.setY(in.readDouble());
		this.setZ(in.readDouble());
	}
}
