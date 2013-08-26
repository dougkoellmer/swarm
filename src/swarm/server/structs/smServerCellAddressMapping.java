package swarm.server.structs;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smU_Blob;
import swarm.server.data.blob.smU_Serialization;
import swarm.server.entities.smE_GridType;

import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;

public class smServerCellAddressMapping extends smCellAddressMapping implements smI_Blob, smI_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	private smE_GridType m_gridType;
	
	/**
	 * Nullary constructor for use by blob manager only.
	 */
	public smServerCellAddressMapping()
	{
		m_gridType = null;
	}
	
	public smServerCellAddressMapping(smE_GridType gridType)
	{
		super();
		
		m_gridType = gridType;
	}
	
	public smServerCellAddressMapping(smE_GridType gridType, smGridCoordinate source)
	{
		super(source);
		
		m_gridType = gridType;
	}
	
	public smE_GridType getGridType()
	{
		return m_gridType;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		((smServerGridCoordinate)m_coordinate).writeExternal(out);
		
		smU_Serialization.writeNullableEnum(m_gridType, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		((smServerGridCoordinate)m_coordinate).readExternal(in);
		
		m_gridType = smU_Serialization.readNullableEnum(smE_GridType.values(), in);
	}

	@Override
	public String getKind()
	{
		return "sm_addy_map";
	}
	
	@Override
	protected void initCoordinate()
	{
		m_coordinate = new smServerGridCoordinate();
	}

	@Override
	public String createBlobKey(smI_Blob blob)
	{
		return smU_Blob.generateKey(blob, m_gridType.getKeyComponent(), m_coordinate.writeString());
	}
	
	@Override
	public smE_BlobCacheLevel getMaximumCacheLevel()
	{
		return smE_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}
