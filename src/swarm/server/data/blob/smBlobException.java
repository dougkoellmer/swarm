package swarm.server.data.blob;

public class smBlobException extends Exception
{
	public smBlobException(String message)
	{
		super(message);
	}
	
	public smBlobException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public smBlobException(Throwable cause)
	{
		super(cause);
	}
}
