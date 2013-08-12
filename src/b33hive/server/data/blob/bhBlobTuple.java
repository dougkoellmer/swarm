package b33hive.server.data.blob;

class bhBlobTuple
{
	final bhI_BlobKey m_keySource;
	final Class<? extends bhI_Blob> m_blobType;
	
	bhBlobTuple(bhI_BlobKey keySource, Class<? extends bhI_Blob> blobType)
	{
		m_keySource = keySource;
		m_blobType = blobType;
	}
}
