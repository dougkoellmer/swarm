package swarm.shared.entities;

import java.util.Iterator;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.reflection.smI_Callback;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;


/**
 * ...
 * @author
 */
public abstract class smA_User extends smA_JsonEncodable
{
	private static final smCellAddressMapping s_utilMapping1 = new smCellAddressMapping();
	
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
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		//--- DRK > For now not concerned with last position...not sure how it should behave.
		//getLastPosition().readJson(json);
		
		m_editingPermission = factory.getHelper().getEnum(json, smE_JsonKey.editingPermission, smE_EditingPermission.values());
		
		smU_User.readUserCells(factory, json, s_utilMapping1, new smI_Callback()
		{
			@Override
			public void invoke(Object... args)
			{
				smCellAddressMapping mapping = new smCellAddressMapping(s_utilMapping1);
				smA_User.this.justReadMappingFromJson(mapping);
			}
		});
	}
	
	public abstract boolean isCellOwner(smGridCoordinate coordinate);
	
	public abstract smPoint getLastPosition();
	
	protected abstract void justReadMappingFromJson(smCellAddressMapping mapping);
}