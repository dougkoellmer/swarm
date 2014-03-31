package swarm.client.app;

import swarm.client.entities.A_ClientUser;
import swarm.client.view.ViewController;
import swarm.shared.app.AppConfig;
import swarm.shared.entities.A_Grid;

public class ClientAppConfig extends AppConfig
{
	public double floatingHistoryUpdateFreq_seconds = .75;
	public double cellHudHeight;
	public double minSnapTime = .8;
	public double snapTimeRange = 1;
	public int framerate_milliseconds = 33;
	public double backOffDistance;
	
	public boolean useVirtualSandbox = true;
	
	public int addressCacheSize = 1024;
	public double addressCacheExpiration_seconds = 60 * 5;
	
	public int cellSizeCacheSize = 1024;
	public double cellSizeCacheExpiration_seconds = 60 * 5;
	
	public int codeCacheSize = 128;
	public double codeCacheExpiration_seconds = 60 * 5;
	
	public A_ClientUser user;
	public A_Grid grid;
	public ViewController viewController;
	
	public int maxSubCellDimension = 1;
}
