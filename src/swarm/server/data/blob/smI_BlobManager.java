package swarm.server.data.blob;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;


public interface smI_BlobManager
{	
	void putBlob(smI_BlobKey keySource, smI_Blob blob) throws bhBlobException, ConcurrentModificationException;
	
	void putBlobAsync(smI_BlobKey keySource, smI_Blob blob) throws bhBlobException;
	
	<T extends smI_Blob> T getBlob(smI_BlobKey keySource, Class<? extends T> T) throws bhBlobException;
	
	Map<smI_BlobKey, smI_Blob> getBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws bhBlobException;

	void putBlobs(Map<smI_BlobKey, smI_Blob> values) throws bhBlobException;
	
	void putBlobsAsync(Map<smI_BlobKey, smI_Blob> values) throws bhBlobException;
	
	void deleteBlob(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws bhBlobException;
	
	void deleteBlobAsync(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws bhBlobException;
	
	void deleteBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws bhBlobException;
	
	void deleteBlobsAsync(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws bhBlobException;
	
	Map<smI_BlobKey, smI_Blob> performQuery(smBlobQuery query) throws bhBlobException;
}
