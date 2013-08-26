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

public class smU_Blob
{
	private smU_Blob()
	{
		
	}
	
	public static String generateKey(smI_Blob blob, String ... keyComponents)
	{
		String key = blob.getKind();
		
		for( int i = 0; i < keyComponents.length; i++ )
		{
			key += "_" + keyComponents[i];
		}
		
		return key;
	}
	
	public static smI_Blob createBlobInstance(Class<? extends smI_Blob> T) throws smBlobException
	{
		smI_Blob blob = null;
		
		try
		{
			blob = T.newInstance();
		}
		catch (IllegalAccessException e)
		{
			throw new smBlobException("Problem creating blob instance.", e);
		}
		catch (InstantiationException e)
		{
			throw new smBlobException("Problem creating blob instance.", e);
		}
		
		return blob;
	}
	
	static byte[] convertToBytes(smI_Blob blob) throws smBlobException
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
			throw new smBlobException("Problem writing blob to bytes.", e);
		}
		
		return blobBytes;
	}
	
	static void readBytes(smI_Blob blob, Entity entity) throws smBlobException
	{
		Blob blobData = (Blob) entity.getProperty(smS_Blob.DATA_FIELD_NAME);
		
		byte[] blobBytes = blobData.getBytes();
		
		smU_Blob.readBytes(blob, blobBytes);
	}
	
	static void readBytes(smI_Blob blob, byte[] blobBytes) throws smBlobException
	{
		try
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(blobBytes);
			ObjectInput input = new ObjectInputStream(stream);
			blob.readExternal(input);
		}
		catch(Exception e)
		{
			throw new smBlobException("Problem reading blob from bytes.", e);
		}
	}
}
