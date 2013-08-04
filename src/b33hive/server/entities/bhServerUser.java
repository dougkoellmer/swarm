package b33hive.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhU_Serialization;
import b33hive.server.structs.bhDate;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.server.structs.bhServerPoint;
import b33hive.shared.entities.bhA_User;
import b33hive.shared.entities.bhE_EditingPermission;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.utils.bhU_Singletons;


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
		return bhS_BlobKeyPrefix.USER_PREFIX;
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
		bhA_JsonFactory jsonFactory = bhU_Singletons.get(bhA_JsonFactory.class);
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
		
		bhJsonHelper.getInstance().putJsonArray(json, bhE_JsonKey.ownedCoordinates, ownedCoordinates);
	}
}