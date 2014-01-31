package swarm.server.data.blob;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;


public interface I_BlobManager
{	
	void putBlob(I_BlobKey keySource, I_Blob blob) throws BlobException, ConcurrentModificationException;
	
	void putBlobAsync(I_BlobKey keySource, I_Blob blob) throws BlobException;
	
	<T extends I_Blob> T getBlob(I_BlobKey keySource, Class<? extends T> T) throws BlobException;
	
	Map<I_BlobKey, I_Blob> getBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException;

	void putBlobs(Map<I_BlobKey, I_Blob> values) throws BlobException;
	
	void putBlobsAsync(Map<I_BlobKey, I_Blob> values) throws BlobException;
	
	void deleteBlob(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException;
	
	void deleteBlobAsync(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException;
	
	void deleteBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException;
	
	void deleteBlobsAsync(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException;
	
	Map<I_BlobKey, I_Blob> performQuery(BlobQuery query) throws BlobException;
}
