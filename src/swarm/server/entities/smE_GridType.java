package swarm.server.entities;

import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smU_Blob;

public enum smE_GridType implements smI_BlobKey
{
	//--- This enum's name field is used as part of a key for database blobs...think twice before changing their names.
	ACTIVE,
	INACTIVE;
	
	private final String m_keyComponent;
	
	private smE_GridType()
	{
		m_keyComponent = this.name().toLowerCase();
	}
	
	public String getKeyComponent()
	{
		return m_keyComponent;
	}
	
	@Override
	public String createBlobKey(smI_Blob blob)
	{
		return smU_Blob.generateKey(blob, getKeyComponent());
	}
}
