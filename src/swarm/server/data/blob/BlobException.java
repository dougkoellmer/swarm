package swarm.server.data.blob;

public class BlobException extends Exception
{
	public BlobException(String message)
	{
		super(message);
	}
	
	public BlobException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public BlobException(Throwable cause)
	{
		super(cause);
	}
}
