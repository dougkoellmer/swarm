package com.b33hive.server.data.blob;

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

public class bhU_Blob
{
	private bhU_Blob()
	{
		
	}
	
	public static String generateKey(bhI_Blob blob, String ... keyComponents)
	{
		String key = blob.getKind();
		
		for( int i = 0; i < keyComponents.length; i++ )
		{
			key += "_" + keyComponents[i];
		}
		
		return key;
	}
	
	public static bhI_Blob createBlobInstance(Class<? extends bhI_Blob> T) throws bhBlobException
	{
		bhI_Blob blob = null;
		
		try
		{
			blob = T.newInstance();
		}
		catch (IllegalAccessException e)
		{
			throw new bhBlobException("Problem creating blob instance.", e);
		}
		catch (InstantiationException e)
		{
			throw new bhBlobException("Problem creating blob instance.", e);
		}
		
		return blob;
	}
	
	static byte[] convertToBytes(bhI_Blob blob) throws bhBlobException
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
			throw new bhBlobException("Problem writing blob to bytes.", e);
		}
		
		return blobBytes;
	}
	
	static void readBytes(bhI_Blob blob, Entity entity) throws bhBlobException
	{
		Blob blobData = (Blob) entity.getProperty(bhS_Blob.DATA_FIELD_NAME);
		
		byte[] blobBytes = blobData.getBytes();
		
		bhU_Blob.readBytes(blob, blobBytes);
	}
	
	static void readBytes(bhI_Blob blob, byte[] blobBytes) throws bhBlobException
	{
		try
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(blobBytes);
			ObjectInput input = new ObjectInputStream(stream);
			blob.readExternal(input);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Problem reading blob from bytes.", e);
		}
	}
}
