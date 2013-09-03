package swarm.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import swarm.client.app.smE_Platform;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smU_Blob;
import swarm.server.data.blob.smU_Serialization;

import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smE_CellAddressParseError;

public class smServerCellAddress extends smCellAddress implements Externalizable, smI_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	public smServerCellAddress(smA_JsonFactory jsonFactory, smI_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public smServerCellAddress(String rawAddress)
	{
		super(rawAddress);
	}

	public smServerCellAddress()
	{
		super();
	}
	
	public boolean isValid()
	{
		return this.getParseError() == smE_CellAddressParseError.NO_ERROR;
	}
	
	public static smServerCellAddress createInstance(String usernamePart, String cellPart)
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
		
		return new smServerCellAddress(rawAddress);
	}

	@Override
	public String createBlobKey(smI_Blob blob)
	{
		return smU_Blob.generateKey(blob, this.getRawAddress());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		smU_Serialization.writeNullableString(this.getCasedRawAddress(), out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		String rawAddress = smU_Serialization.readNullableString(in);
		
		this.init(rawAddress);
	}
}
