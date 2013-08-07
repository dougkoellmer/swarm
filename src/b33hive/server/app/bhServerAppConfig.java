package b33hive.server.app;

import b33hive.server.handlers.admin.bhI_HomeCellCreator;

public class bhServerAppConfig
{
	public String accountDatabase;
	public String telemetryDatabase;
	
	public Class<? extends bhI_HomeCellCreator> T_homeCellCreator;
}
