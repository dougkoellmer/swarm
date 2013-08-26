package swarm.shared.entities;

import java.util.Iterator;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
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
public abstract class smA_User extends smA_JsonEncodable
{
	private smE_EditingPermission m_editingPermission = smE_EditingPermission.OWNED_CELLS;
	
	public smE_EditingPermission getEditingPermission()
	{
		return m_editingPermission;
	}
	
	public void setEditingPermission(smE_EditingPermission permission)
	{
		m_editingPermission = permission;
	}
	
	@Override
	public void readJson(smI_JsonObject json)
	{
		//--- DRK > For now not concerned with last position...not sure how it should behave.
		//getLastPosition().readJson(json);
		
		smI_JsonArray jsonCells = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.ownedCoordinates);
		
		sm.jsonFactory.getHelper().putEnum(json, smE_JsonKey.editingPermission, m_editingPermission);
		
		for( int i = 0; i < jsonCells.getSize(); i++ )
		{
			smCellAddressMapping mapping = null;
			
			try
			{
				mapping = new smCellAddressMapping();
				mapping.readJson(jsonCells.getObject(i));
				
				this.justReadMappingFromJson(mapping);
			}
			catch(Exception e)
			{
				e.toString();
			}
		}
	}
	
	public abstract boolean isCellOwner(smGridCoordinate coordinate);
	
	public abstract smPoint getLastPosition();
	
	protected abstract void justReadMappingFromJson(smCellAddressMapping mapping);
}