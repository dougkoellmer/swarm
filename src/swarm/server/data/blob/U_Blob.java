package swarm.server.data.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;

public class U_Blob
{
	private U_Blob()
	{
		
	}
	
	public static String generateKey(I_Blob blob, String ... keyComponents)
	{
		String key = blob.getKind();
		
		for( int i = 0; i < keyComponents.length; i++ )
		{
			key += "_" + keyComponents[i];
		}
		
		return key;
	}
	
	public static I_Blob createBlobInstance(Class<? extends I_Blob> T) throws BlobException
	{
		I_Blob blob = null;
		
		try
		{
			blob = T.newInstance();
		}
		catch (IllegalAccessException e)
		{
			throw new BlobException("Problem creating blob instance.", e);
		}
		catch (InstantiationException e)
		{
			throw new BlobException("Problem creating blob instance.", e);
		}
		
		return blob;
	}
	
	static byte[] convertToBytes(I_Blob blob) throws BlobException
	{
		byte[] blobBytes = null;
		
		try
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutput output = new ObjectOutputStream(stream);
			blob.writeExternal(output);
			output.flush();
			blobBytes = stream.toByteArray();
		}
		catch(Exception e)
		{
			throw new BlobException("Problem writing blob to bytes.", e);
		}
		
		return blobBytes;
	}
	
	static void readBytes(I_Blob blob, Entity entity) throws BlobException
	{
		Blob blobData = (Blob) entity.getProperty(S_Blob.DATA_FIELD_NAME);
		
		byte[] blobBytes = blobData.getBytes();
		
		U_Blob.readBytes(blob, blobBytes);
	}
	
	static void readBytes(I_Blob blob, byte[] blobBytes) throws BlobException
	{
		try
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(blobBytes);
			ObjectInput input = new ObjectInputStream(stream);
			blob.readExternal(input);
		}
		catch(Exception e)
		{
			throw new BlobException("Problem reading blob from bytes.", e);
		}
	}
}
