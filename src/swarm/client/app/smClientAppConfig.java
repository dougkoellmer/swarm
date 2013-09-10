package swarm.client.app;

import swarm.client.entities.smA_ClientUser;
import swarm.client.view.smViewController;
import swarm.shared.app.smAppConfig;
import swarm.shared.entities.smA_Grid;

public class smClientAppConfig extends smAppConfig
{
	public double floatingHistoryUpdateFreq_seconds = .75;
	public double cellHudHeight;
	public double minSnapTime = .8;
	public double maxSnapTime = 1.75;
	public int framerate_milliseconds = 33;
	public double backOffDistance;
	
	public boolean useVirtualSandbox = true;
	
	public int addressCacheSize = 1024;
	public double addressCacheExpiration_seconds = 60 * 5;
	
	public int codeCacheSize = 128;
	public double codeCacheExpiration_seconds = 60 * 5;
	
	public smA_ClientUser user;
	public smA_Grid grid;
	public smViewController viewController;
}
