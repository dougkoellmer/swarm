package b33hive.shared.transaction;

public enum bhE_ResponseError
{
	//--- DRK > The first two here must never be changed, ever....EVER.
	NO_ERROR,
	VERSION_MISMATCH,
	
	//--- DRK > Set only by client transaction manager.
	CLIENT_ERROR, // e.g. no network connection maybe
	HTTP_ERROR,		// can be caused by any bad http status code returned.
	
	//--- DRK > Set only by server transaction manager.
	REQUEST_READ_ERROR,
	UNKNOWN_PATH,
	SERVER_EXCEPTION,
	HANDLER_EXCEPTION,
	DEFERRED_BUT_NEVER_HANDLED,  // indicates that a request handler set response error to DEFERRED but never resolved it.
	
	//--- DRK > Set only by request handlers on server.
	NOT_AUTHENTICATED,
	NOT_AUTHORIZED,
	SERVICE_EXCEPTION, // when some backend service fails unexpectedly, like database, email, etc.
	BAD_INPUT, // set when invalid/missing data is given to a request handler
	BAD_STATE, // set when request assumes something is in a certain state, but server disagrees.
	
	//--- DRK > Set only by request handlers on server, indicating a "soft" error that is recoverable.
	DEFERRED,
	REDUNDANT;
}
