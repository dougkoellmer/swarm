package swarm.shared.entities;
import swarm.client.managers.smCellAddressManager;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.reflection.smI_Callback;
import swarm.shared.structs.smCellAddressMapping;


public class smU_User
{
	public static void readUserCells(smA_JsonFactory factory, smI_JsonObject json, smCellAddressMapping mapping_out, smI_Callback callback)
	{
		smI_JsonArray jsonCells = factory.getHelper().getJsonArray(json, smE_JsonKey.ownedCoordinates);
		for( int i = 0; i < jsonCells.getSize(); i++ )
		{
			smCellAddressMapping mapping = null;
			
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
