package swarm.shared.transaction;

import swarm.shared.app.sm;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public class bhU_RequestBatch
{
	public static interface I_JsonReadDelegate
	{
		void onRequestFound(bhI_JsonObject requestJson);
	}
	
	public static boolean isBatch(bhI_JsonObject json)
	{
		return sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.requestList) != null;
	}
	
	public static int getBatchSize(bhI_JsonObject json)
	{
		return sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.requestList).getSize();
	}
	
	public static void readRequestList(bhI_JsonObject json, I_JsonReadDelegate delegate)
	{
		bhI_JsonArray requestList = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.requestList);
		
		for( int i = 0; i < requestList.getSize(); i++ )
		{
			delegate.onRequestFound(requestList.getObject(i));
		}
	}
}
