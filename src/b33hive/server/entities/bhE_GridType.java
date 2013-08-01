package com.b33hive.server.entities;

import com.b33hive.server.data.blob.bhI_Blob;
import com.b33hive.server.data.blob.bhI_BlobKeySource;
import com.b33hive.server.data.blob.bhU_Blob;

public enum bhE_GridType implements bhI_BlobKeySource
{
	//--- This enum's name field is used as part of a key for database blobs...think twice before changing their names.
	ACTIVE,
	INACTIVE;
	
	private final String m_keyComponent;
	
	private bhE_GridType()
	{
		m_keyComponent = this.name().toLowerCase();
	}
	
	public String getKeyComponent()
	{
		return m_keyComponent;
	}
	
	@Override
	public String createBlobKey(bhI_Blob blob)
	{
		return bhU_Blob.generateKey(blob, getKeyComponent());
	}
}
