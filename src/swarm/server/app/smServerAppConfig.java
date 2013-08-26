package swarm.server.app;

import swarm.server.handlers.admin.smI_HomeCellCreator;
import swarm.shared.app.smAppConfig;

public class smServerAppConfig extends bhAppConfig
{
	public String databaseUrl;
	public String accountsDatabase;
	public String telemetryDatabase;
	public double startingZ;
	public int gridExpansionDelta;
	
	public String mainPage;
	
	public String privateRecaptchaKey;
	
	public Class<? extends smI_HomeCellCreator> T_homeCellCreator;
}
