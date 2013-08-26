package swarm.server.data.blob;

public class smBlobQuery
{
	final Class<? extends smI_Blob> m_blobType;
	
	public smBlobQuery(Class<? extends smI_Blob> blobType)
	{
		m_blobType = blobType;
	}
	
	public Class<? extends smI_Blob> getBlobType()
	{
		return m_blobType;
	}
}
