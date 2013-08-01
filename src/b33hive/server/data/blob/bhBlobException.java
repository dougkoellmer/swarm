package com.b33hive.server.data.blob;

public class bhBlobException extends Exception
{
	public bhBlobException(String message)
	{
		super(message);
	}
	
	public bhBlobException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public bhBlobException(Throwable cause)
	{
		super(cause);
	}
}
