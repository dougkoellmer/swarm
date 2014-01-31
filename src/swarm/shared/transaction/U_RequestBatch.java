package swarm.shared.transaction;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class U_RequestBatch
{
	public static interface I_JsonReadDelegate
	{
		void onRequestFound(I_JsonObject requestJson);
	}
	
	public static boolean isBatch(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		return jsonFactory.getHelper().getJsonArray(json, E_JsonKey.requestList) != null;
	}
	
	public static int getBatchSize(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		return jsonFactory.getHelper().getJsonArray(json, E_JsonKey.requestList).getSize();
	}
	
	public static void readRequestList(A_JsonFactory jsonFactory, I_JsonObject json, I_JsonReadDelegate delegate)
	{
		I_JsonArray requestList = jsonFactory.getHelper().getJsonArray(json, E_JsonKey.requestList);
		
		for( int i = 0; i < requestList.getSize(); i++ )
		{
			delegate.onRequestFound(requestList.getObject(i));
		}
	}
}
