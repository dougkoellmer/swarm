package swarm.server.structs;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhU_Blob;
import swarm.server.data.blob.bhU_Serialization;
import swarm.server.entities.bhE_GridType;

import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhGridCoordinate;

public class bhServerCellAddressMapping extends bhCellAddressMapping implements bhI_Blob, bhI_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	private bhE_GridType m_gridType;
	
	/**
	 * Nullary constructor for use by blob manager only.
	 */
	public bhServerCellAddressMapping()
	{
		m_gridType = null;
	}
	
	public bhServerCellAddressMapping(bhE_GridType gridType)
	{
		super();
		
		m_gridType = gridType;
	}
	
	public bhServerCellAddressMapping(bhE_GridType gridType, bhGridCoordinate source)
	{
		super(source);
		
		m_gridType = gridType;
	}
	
	public bhE_GridType getGridType()
	{
		return m_gridType;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		((bhServerGridCoordinate)m_coordinate).writeExternal(out);
		
		bhU_Serialization.writeNullableEnum(m_gridType, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		((bhServerGridCoordinate)m_coordinate).readExternal(in);
		
		m_gridType = bhU_Serialization.readNullableEnum(bhE_GridType.values(), in);
	}

	@Override
	public String getKind()
	{
		return "sm_addy_map";
	}
	
	@Override
	protected void initCoordinate()
	{
		m_coordinate = new bhServerGridCoordinate();
	}

	@Override
	public String createBlobKey(bhI_Blob blob)
	{
		return bhU_Blob.generateKey(blob, m_gridType.getKeyComponent(), m_coordinate.writeString());
	}
	
	@Override
	public bhE_BlobCacheLevel getMaximumCacheLevel()
	{
		return bhE_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}
