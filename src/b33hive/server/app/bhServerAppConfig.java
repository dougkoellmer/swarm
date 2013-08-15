package b33hive.server.app;

import b33hive.server.handlers.admin.bhI_HomeCellCreator;
import b33hive.shared.app.bhAppConfig;

public class bhServerAppConfig extends bhAppConfig
{
	public String accountDatabase;
	public String telemetryDatabase;
	public double startingZ;
	public int gridExpansionDelta;
	
	public String mainPage;

	public String publicRecaptchaKey;
	public String privateRecaptchaKey;
	
	public Class<? extends bhI_HomeCellCreator> T_homeCellCreator;
}
