package b33hive.server.data.blob;

import java.io.Externalizable;
import java.util.Map;

public interface bhI_Blob extends Externalizable
{
	bhE_BlobCacheLevel getMaximumCacheLevel();
	
	String getKind();
	
	Map<String, Object> getQueryableProperties();
}
