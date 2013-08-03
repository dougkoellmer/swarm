package b33hive.shared.transaction;

import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhU_RequestBatch
{
	public static interface I_JsonReadDelegate
	{
		void onRequestFound(bhI_JsonObject requestJson);
	}
	
	public static boolean isBatch(bhI_JsonObject json)
	{
		return bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.requestList) != null;
	}
	
	public static int getBatchSize(bhI_JsonObject json)
	{
		return bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.requestList).getSize();
	}
	
	public static void readRequestList(bhI_JsonObject json, I_JsonReadDelegate delegate)
	{
		bhI_JsonArray requestList = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.requestList);
		
		for( int i = 0; i < requestList.getSize(); i++ )
		{
			delegate.onRequestFound(requestList.getObject(i));
		}
	}
}
