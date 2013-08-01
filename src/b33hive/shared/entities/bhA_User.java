package com.b33hive.shared.entities;

import java.util.Iterator;

import com.b33hive.shared.json.bhA_JsonEncodable;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonArray;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.structs.bhPoint;


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
		
		bhI_JsonArray jsonCells = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.ownedCoordinates);
		
		bhJsonHelper.getInstance().putEnum(json, bhE_JsonKey.editingPermission, m_editingPermission);
		
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