package swarm.shared.transaction;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smU_RequestBatch
{
	public static interface I_JsonReadDelegate
	{
		void onRequestFound(smI_JsonObject requestJson);
	}
	
	public static boolean isBatch(smI_JsonObject json)
	{
		return factory.getHelper().getJsonArray(json, smE_JsonKey.requestList) != null;
	}
	
	public static int getBatchSize(smI_JsonObject json)
	{
		return factory.getHelper().getJsonArray(json, smE_JsonKey.requestList).getSize();
	}
	
	public static void readRequestList(smI_JsonObject json, I_JsonReadDelegate delegate)
	{
		smI_JsonArray requestList = factory.getHelper().getJsonArray(json, smE_JsonKey.requestList);
		
		for( int i = 0; i < requestList.getSize(); i++ )
		{
			delegate.onRequestFound(requestList.getObject(i));
		}
	}
}
