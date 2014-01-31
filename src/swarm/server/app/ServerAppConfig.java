package swarm.server.app;

import swarm.server.handlers.admin.I_HomeCellCreator;
import swarm.shared.app.AppConfig;
import swarm.shared.structs.GridCoordinate;

public class ServerAppConfig extends AppConfig
{
	public String databaseUrl;
	public String accountsDatabase;
	public String telemetryDatabase;
	public double startingZ;
	public GridCoordinate startingCoord = null;
	public int gridExpansionDelta;
	
	public String mainPage;
	
	public String privateRecaptchaKey;
	
	public Class<? extends I_HomeCellCreator> T_homeCellCreator;
}
