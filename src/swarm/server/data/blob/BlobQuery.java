package swarm.server.data.blob;

public class BlobQuery
{
	final Class<? extends I_Blob> m_blobType;
	
	public BlobQuery(Class<? extends I_Blob> blobType)
	{
		m_blobType = blobType;
	}
	
	public Class<? extends I_Blob> getBlobType()
	{
		return m_blobType;
	}
}
