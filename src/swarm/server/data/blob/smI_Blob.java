package swarm.server.data.blob;

import java.io.Externalizable;
import java.util.Map;

public interface smI_Blob extends Externalizable
{
	smE_BlobCacheLevel getMaximumCacheLevel();
	
	String getKind();
	
	Map<String, Object> getQueryableProperties();
}
