package swarm.shared.entities;
import swarm.client.managers.CellAddressManager;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.reflection.I_Callback;
import swarm.shared.structs.CellAddressMapping;


public class U_User
{
	public static void readUserCells(A_JsonFactory factory, I_JsonObject json, CellAddressMapping mapping_out, I_Callback callback)
	{
		I_JsonArray jsonCells = factory.getHelper().getJsonArray(json, E_JsonKey.ownedCoordinates);
		for( int i = 0; i < jsonCells.getSize(); i++ )
		{
			try
			{
				mapping_out.readJson(factory, jsonCells.getObject(i));
				
				callback.invoke();
			}
			catch(Exception e)
			{
				e.toString();
			}
		}
	}
}
