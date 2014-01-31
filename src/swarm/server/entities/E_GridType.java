package swarm.server.entities;

import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;

public enum E_GridType implements I_BlobKey
{
	//--- This enum's name field is used as part of a key for database blobs...think twice before changing their names.
	ACTIVE,
	INACTIVE;
	
	private final String m_keyComponent;
	
	private E_GridType()
	{
		m_keyComponent = this.name().toLowerCase();
	}
	
	public String getKeyComponent()
	{
		return m_keyComponent;
	}
	
	@Override
	public String createBlobKey(I_Blob blob)
	{
		return U_Blob.generateKey(blob, getKeyComponent());
	}
}
