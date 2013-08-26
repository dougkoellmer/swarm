package swarm.server.data.blob;

class smBlobTuple
{
	final smI_BlobKey m_keySource;
	final Class<? extends smI_Blob> m_blobType;
	
	bhBlobTuple(smI_BlobKey keySource, Class<? extends smI_Blob> blobType)
	{
		m_keySource = keySource;
		m_blobType = blobType;
	}
}
