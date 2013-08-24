package swarm.shared.entities;

import java.util.Iterator;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
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
public abstract class bhA_User extends bhA_JsonEncodable
{
	private bhE_EditingPermission m_editingPermission = bhE_EditingPermission.OWNED_CELLS;
	
	public bhE_EditingPermission getEditingPermission()
	{
		return m_editingPermission;
	}
	
	public void setEditingPermission(bhE_EditingPermission permission)
	{
		m_editingPermission = permission;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		//--- DRK > For now not concerned with last position...not sure how it should behave.
		//getLastPosition().readJson(json);
		
		bhI_JsonArray jsonCells = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.ownedCoordinates);
		
		sm.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.editingPermission, m_editingPermission);
		
		for( int i = 0; i < jsonCells.getSize(); i++ )
		{
			bhCellAddressMapping mapping = null;
			
			try
			{
				mapping = new bhCellAddressMapping();
				mapping.readJson(jsonCells.getObject(i));
				
				this.justReadMappingFromJson(mapping);
			}
			catch(Exception e)
			{
				e.toString();
			}
		}
	}
	
	public abstract boolean isCellOwner(bhGridCoordinate coordinate);
	
	public abstract bhPoint getLastPosition();
	
	protected abstract void justReadMappingFromJson(bhCellAddressMapping mapping);
}