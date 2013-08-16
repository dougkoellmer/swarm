package b33hive.client.app;

import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.ui.bhViewController;
import b33hive.shared.app.bhAppConfig;
import b33hive.shared.entities.bhA_Grid;

public class bhClientAppConfig extends bhAppConfig
{
	public double floatingHistoryUpdateFreq_seconds;
	public double cellHudHeight;
	public double minSnapTime;
	public double snapTimeRange;
	public int framerate_milliseconds;
	public double backOffDistance;
	
	public int addressCacheSize;
	public double addressCacheExpiration_seconds;
	
	public int codeCacheSize;
	public double codeCacheExpiration_seconds;
	
	public bhA_ClientUser user;
	public bhA_Grid grid;
	public bhViewController viewController;
}
