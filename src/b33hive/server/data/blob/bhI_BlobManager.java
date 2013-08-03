package b33hive.server.data.blob;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;


public interface bhI_BlobManager
{	
	void putBlob(bhI_BlobKeySource keySource, bhI_Blob blob) throws bhBlobException, ConcurrentModificationException;
	
	void putBlobAsync(bhI_BlobKeySource keySource, bhI_Blob blob) throws bhBlobException;
	
	<T extends bhI_Blob> T getBlob(bhI_BlobKeySource keySource, Class<? extends T> T) throws bhBlobException;
	
	Map<bhI_BlobKeySource, bhI_Blob> getBlobs(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException;

	void putBlobs(Map<bhI_BlobKeySource, bhI_Blob> values) throws bhBlobException;
	
	void putBlobsAsync(Map<bhI_BlobKeySource, bhI_Blob> values) throws bhBlobException;
	
	void deleteBlob(bhI_BlobKeySource keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException;
	
	void deleteBlobAsync(bhI_BlobKeySource keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException;
	
	void deleteBlobs(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException;
	
	void deleteBlobsAsync(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException;
	
	Map<bhI_BlobKeySource, bhI_Blob> performQuery(bhBlobQuery query) throws bhBlobException;
}
