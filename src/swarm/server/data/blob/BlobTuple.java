package swarm.server.data.blob;

class BlobTuple
{
	final I_BlobKey m_keySource;
	final Class<? extends I_Blob> m_blobType;
	
	BlobTuple(I_BlobKey keySource, Class<? extends I_Blob> blobType)
	{
		m_keySource = keySource;
		m_blobType = blobType;
	}
}
