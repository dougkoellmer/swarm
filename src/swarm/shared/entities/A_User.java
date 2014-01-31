package swarm.shared.entities;

import java.util.Iterator;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.reflection.I_Callback;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;


/**
 * ...
 * @author
 */
public abstract class A_User extends A_JsonEncodable
{
	private static final CellAddressMapping s_utilMapping1 = new CellAddressMapping();
	
	private E_EditingPermission m_editingPermission = E_EditingPermission.OWNED_CELLS;
	
	public E_EditingPermission getEditingPermission()
	{
		return m_editingPermission;
	}
	
	public void setEditingPermission(E_EditingPermission permission)
	{
		m_editingPermission = permission;
	}
	
	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		//--- DRK > For now not concerned with last position...not sure how it should behave.
		//getLastPosition().readJson(json);
		
		m_editingPermission = factory.getHelper().getEnum(json, E_JsonKey.editingPermission, E_EditingPermission.values());
		
		U_User.readUserCells(factory, json, s_utilMapping1, new I_Callback()
		{
			@Override
			public void invoke(Object... args)
			{
				CellAddressMapping mapping = new CellAddressMapping(s_utilMapping1);
				A_User.this.justReadMappingFromJson(mapping);
			}
		});
	}
	
	public abstract boolean isCellOwner(GridCoordinate coordinate);
	
	public abstract Point getLastPosition();
	
	protected abstract void justReadMappingFromJson(CellAddressMapping mapping);
}