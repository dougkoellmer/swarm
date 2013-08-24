package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhU_Serialization;
import swarm.server.structs.bhDate;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.server.structs.bhServerPoint;
import swarm.shared.app.sm;
import swarm.shared.entities.bhA_User;
import swarm.shared.entities.bhE_EditingPermission;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;



/**
 * ...
 * @author 
 */

public class bhServerUser extends bhA_User implements bhI_Blob
{
	private static final int EXTERNAL_VERSION = 1;
	
	private final bhServerPoint m_lastPosition = new bhServerPoint();
	private final ArrayList<bhServerCellAddressMapping> m_ownedCells = new ArrayList<bhServerCellAddressMapping>();
	
	private final bhDate m_lastUpdated = new bhDate();
	
	public bhServerUser()
	{
	}
	
	public void addOwnedCell(bhServerCellAddressMapping mapping)
	{
		m_ownedCells.add(mapping);
	}
	
	public Iterator<bhServerCellAddressMapping> getOwnedCells()
	{
		return m_ownedCells.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		m_lastPosition.writeExternal(out);
		bhU_Serialization.writeArrayList(m_ownedCells, out);
		
		m_lastUpdated.writeExternal(out);
		
		bhU_Serialization.writeNullableEnum(this.getEditingPermission(), out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_lastPosition.readExternal(in);
		bhU_Serialization.readArrayList(m_ownedCells, bhServerCellAddressMapping.class, in);
		
		m_lastUpdated.readExternal(in);

		bhE_EditingPermission editingPermission = bhU_Serialization.readNullableEnum(bhE_EditingPermission.values(), in);
		this.setEditingPermission(editingPermission);
	}
	
	@Override
	public bhPoint getLastPosition()
	{
		return m_lastPosition;
	}
	
	public bhServerGridCoordinate getHomeCoordinate()
	{
		return (bhServerGridCoordinate) m_ownedCells.get(0).getCoordinate();
	}
	
	public int getCellCount()
	{
		return m_ownedCells.size();
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_ownedCells.clear();
		
		super.readJson(json);
	}

	@Override
	protected void justReadMappingFromJson(bhCellAddressMapping mapping)
	{
		m_ownedCells.add((bhServerCellAddressMapping) mapping);
	}

	@Override
	public boolean isCellOwner(bhGridCoordinate coordinate)
	{
		for( int i = 0; i < m_ownedCells.size(); i++ )
		{
			if( coordinate.isEqualTo(m_ownedCells.get(i)) )
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
	public bhE_BlobCacheLevel getMaximumCacheLevel()
	{
		return bhE_BlobCacheLevel.PERSISTENT;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhA_JsonFactory jsonFactory = sm.jsonFactory;
		bhI_JsonArray ownedCoordinates = jsonFactory.createJsonArray();
		for( int i = 0; i < m_ownedCells.size(); i++ )
		{
			bhServerCellAddressMapping mapping = m_ownedCells.get(i);
			
			if( mapping.getGridType() != bhE_GridType.ACTIVE )
			{
				continue;
			}
			
			bhI_JsonObject coordJson = m_ownedCells.get(i).getCoordinate().writeJson();
			ownedCoordinates.addObject(coordJson);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.ownedCoordinates, ownedCoordinates);
	}
}