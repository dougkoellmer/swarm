package swarm.shared.transaction;

public enum smE_RequestPathBlock
{	
	LIB,
	LIB_TELEMETRY,
	LIB_ADMIN,
	LIB_DEBUG,
	APP,
	APP_TELEMETRY,
	APP_ADMIN,
	APP_DEBUG;
	
	public static final int BLOCK_SIZE = 1000;
	
	public int getPathId(Enum requestPath)
	{
		return BLOCK_SIZE*this.ordinal() + requestPath.ordinal();
	}
}
