package com.b33hive.server.data.blob;

public class bhBlobQuery
{
	final Class<? extends bhI_Blob> m_blobType;
	
	public bhBlobQuery(Class<? extends bhI_Blob> blobType)
	{
		m_blobType = blobType;
	}
	
	public Class<? extends bhI_Blob> getBlobType()
	{
		return m_blobType;
	}
}
