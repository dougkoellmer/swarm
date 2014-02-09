package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.U_Serialization;
import swarm.server.structs.SerializableDate;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.server.structs.ServerPoint;
import swarm.shared.app.BaseAppContext;
import swarm.shared.entities.A_User;
import swarm.shared.entities.E_EditingPermission;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;



/**
 * ...
 * @author 
 */

public class ServerUser extends A_User implements I_Blob
{
	private static final int EXTERNAL_VERSION = 1;
	
	private final ServerPoint m_lastPosition = new ServerPoint();
	private final ArrayList<ServerCellAddressMapping> m_ownedCells = new ArrayList<ServerCellAddressMapping>();
	
	private final SerializableDate m_lastUpdated = new SerializableDate();
	
	public ServerUser()
	{
	}
	
	public void addOwnedCell(ServerCellAddressMapping mapping)
	{
		m_ownedCells.add(mapping);
	}
	
	public Iterator<ServerCellAddressMapping> getOwnedCells()
	{
		return m_ownedCells.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		m_lastPosition.writeExternal(out);
		U_Serialization.writeArrayList(m_ownedCells, out);
		
		m_lastUpdated.writeExternal(out);
		
		U_Serialization.writeNullableEnum(this.getEditingPermission(), out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_lastPosition.readExternal(in);
		U_Serialization.readArrayList(m_ownedCells, ServerCellAddressMapping.class, in);
		
		m_lastUpdated.readExternal(in);

		E_EditingPermission editingPermission = U_Serialization.readNullableEnum(E_EditingPermission.values(), in);
		this.setEditingPermission(editingPermission);
	}
	
	@Override
	public Point getLastPosition()
	{
		return m_lastPosition;
	}
	
	public ServerGridCoordinate getHomeCoordinate()
	{
		return (ServerGridCoordinate) m_ownedCells.get(0).getCoordinate();
	}
	
	public int getCellCount()
	{
		return m_ownedCells.size();
	}
	
	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_ownedCells.clear();
		
		super.readJson(json, factory);
	}

	@Override
	protected void justReadMappingFromJson(CellAddressMapping mapping)
	{
		m_ownedCells.add((ServerCellAddressMapping) mapping);
	}

	@Override
	public boolean isCellOwner(GridCoordinate coordinate)
	{
		for( int i = 0; i < m_ownedCells.size(); i++ )
		{
			if( coordinate.isEqualTo(m_ownedCells.get(i).getCoordinate()) )
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String getKind()
	{
		return "sm_user";
	}
	
	@Override
	public E_BlobCacheLevel getMaximumCacheLevel()
	{
		return E_BlobCacheLevel.PERSISTENT;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
	
	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		A_JsonFactory jsonFactory = factory;
		I_JsonArray ownedCoordinates = jsonFactory.createJsonArray();
		for( int i = 0; i < m_ownedCells.size(); i++ )
		{
			ServerCellAddressMapping mapping = m_ownedCells.get(i);
			
			if( mapping.getGridType() != E_GridType.ACTIVE )
			{
				continue;
			}
			
			I_JsonObject coordJson = factory.createJsonObject();
			m_ownedCells.get(i).getCoordinate().writeJson(coordJson, factory);
			ownedCoordinates.addObject(coordJson);
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.ownedCoordinates, ownedCoordinates);
	}
}