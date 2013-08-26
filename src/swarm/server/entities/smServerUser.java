package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smU_Serialization;
import swarm.server.structs.smDate;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerGridCoordinate;
import swarm.server.structs.smServerPoint;
import swarm.shared.app.sm;
import swarm.shared.entities.smA_User;
import swarm.shared.entities.smE_EditingPermission;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;



/**
 * ...
 * @author 
 */

public class smServerUser extends smA_User implements smI_Blob
{
	private static final int EXTERNAL_VERSION = 1;
	
	private final smServerPoint m_lastPosition = new smServerPoint();
	private final ArrayList<smServerCellAddressMapping> m_ownedCells = new ArrayList<smServerCellAddressMapping>();
	
	private final smDate m_lastUpdated = new smDate();
	
	public smServerUser()
	{
	}
	
	public void addOwnedCell(smServerCellAddressMapping mapping)
	{
		m_ownedCells.add(mapping);
	}
	
	public Iterator<smServerCellAddressMapping> getOwnedCells()
	{
		return m_ownedCells.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		m_lastPosition.writeExternal(out);
		smU_Serialization.writeArrayList(m_ownedCells, out);
		
		m_lastUpdated.writeExternal(out);
		
		smU_Serialization.writeNullableEnum(this.getEditingPermission(), out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_lastPosition.readExternal(in);
		smU_Serialization.readArrayList(m_ownedCells, smServerCellAddressMapping.class, in);
		
		m_lastUpdated.readExternal(in);

		smE_EditingPermission editingPermission = smU_Serialization.readNullableEnum(smE_EditingPermission.values(), in);
		this.setEditingPermission(editingPermission);
	}
	
	@Override
	public smPoint getLastPosition()
	{
		return m_lastPosition;
	}
	
	public smServerGridCoordinate getHomeCoordinate()
	{
		return (smServerGridCoordinate) m_ownedCells.get(0).getCoordinate();
	}
	
	public int getCellCount()
	{
		return m_ownedCells.size();
	}
	
	@Override
	public void readJson(smI_JsonObject json)
	{
		m_ownedCells.clear();
		
		super.readJson(json);
	}

	@Override
	protected void justReadMappingFromJson(smCellAddressMapping mapping)
	{
		m_ownedCells.add((smServerCellAddressMapping) mapping);
	}

	@Override
	public boolean isCellOwner(smGridCoordinate coordinate)
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
	public smE_BlobCacheLevel getMaximumCacheLevel()
	{
		return smE_BlobCacheLevel.PERSISTENT;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		smA_JsonFactory jsonFactory = sm.jsonFactory;
		smI_JsonArray ownedCoordinates = jsonFactory.createJsonArray();
		for( int i = 0; i < m_ownedCells.size(); i++ )
		{
			smServerCellAddressMapping mapping = m_ownedCells.get(i);
			
			if( mapping.getGridType() != smE_GridType.ACTIVE )
			{
				continue;
			}
			
			smI_JsonObject coordJson = m_ownedCells.get(i).getCoordinate().writeJson();
			ownedCoordinates.addObject(coordJson);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.ownedCoordinates, ownedCoordinates);
	}
}