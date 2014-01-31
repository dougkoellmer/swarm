package swarm.server.data.blob;

import java.io.Externalizable;
import java.util.Map;

public interface I_Blob extends Externalizable
{
	E_BlobCacheLevel getMaximumCacheLevel();
	
	String getKind();
	
	Map<String, Object> getQueryableProperties();
}
