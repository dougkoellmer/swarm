package swarm.server.structs;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.server.data.blob.U_Serialization;
import swarm.server.entities.E_GridType;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;

public class ServerCellAddressMapping extends CellAddressMapping implements I_Blob, I_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	private E_GridType m_gridType;
	
	/**
	 * Nullary constructor for use by blob manager only.
	 */
	public ServerCellAddressMapping()
	{
		m_gridType = null;
	}
	
	public ServerCellAddressMapping(String value)
	{
		super(value);
		
		m_gridType = E_GridType.ACTIVE;
	}
	
	public ServerCellAddressMapping(E_GridType gridType)
	{
		super();
		
		m_gridType = gridType;
	}
	
	public ServerCellAddressMapping(E_GridType gridType, GridCoordinate source)
	{
		super(source);
		
		m_gridType = gridType;
	}
	
	public ServerCellAddressMapping(E_GridType gridType, I_JsonObject json, A_JsonFactory jsonFactory)
	{
		super();
		
		m_gridType = gridType;
		
		this.readJson(json, jsonFactory);
	}
	
	public E_GridType getGridType()
	{
		return m_gridType;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		((ServerGridCoordinate)m_coordinate).writeExternal(out);
		
		U_Serialization.writeNullableEnum(m_gridType, out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		((ServerGridCoordinate)m_coordinate).readExternal(in);
		
		m_gridType = U_Serialization.readNullableEnum(E_GridType.values(), in);
	}

	@Override
	public String getKind()
	{
		return "sm_addy_map";
	}
	
	@Override
	protected void initCoordinate()
	{
		m_coordinate = new ServerGridCoordinate();
	}

	@Override
	public String createBlobKey(I_Blob blob)
	{
		return U_Blob.generateKey(blob, m_gridType.getKeyComponent(), m_coordinate.writeString());
	}
	
	@Override
	public E_BlobCacheLevel getMaximumCacheLevel()
	{
		return E_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}
