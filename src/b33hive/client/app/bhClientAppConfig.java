package b33hive.client.app;

import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.ui.bhViewController;
import b33hive.shared.app.bhAppConfig;
import b33hive.shared.entities.bhA_Grid;

public class bhClientAppConfig extends bhAppConfig
{
	public double floatingHistoryUpdateFreq_seconds = .75;
	public double cellHudHeight;
	public double minSnapTime = .8;
	public double maxSnapTime = 1.75;
	public int framerate_milliseconds = 33;
	public double backOffDistance;
	
	public int addressCacheSize = 1024;
	public double addressCacheExpiration_seconds = 60 * 5;
	
	public int codeCacheSize = 128;
	public double codeCacheExpiration_seconds = 60 * 5;
	
	public bhA_ClientUser user;
	public bhA_Grid grid;
	public bhViewController viewController;
}
