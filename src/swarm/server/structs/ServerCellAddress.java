package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.client.app.E_Platform;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.server.data.blob.U_Serialization;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_CellAddressParseError;

public class ServerCellAddress extends CellAddress implements Externalizable, I_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	public ServerCellAddress(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public ServerCellAddress(String rawAddress)
	{
		super(rawAddress);
	}
	
	public ServerCellAddress(CellAddress address_copied)
	{
		super(address_copied);
	}

	public ServerCellAddress()
	{
		super();
	}
	
	public boolean isValid()
	{
		return this.getParseError() == E_CellAddressParseError.NO_ERROR;
	}
	
	public static ServerCellAddress createInstance(String usernamePart, String cellPart)
	{
		String rawAddress = null;
		if( cellPart == null )
		{
			rawAddress = usernamePart;
		}
		else
		{
			rawAddress = usernamePart + "/" + cellPart;
		}
		
		return new ServerCellAddress(rawAddress);
	}

	@Override
	public String createBlobKey(I_Blob blob)
	{
		return U_Blob.generateKey(blob, this.getRaw());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		U_Serialization.writeNullableString(this.getCasedRaw(), out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		String rawAddress = U_Serialization.readNullableString(in);
		
		this.init(rawAddress);
	}
}
