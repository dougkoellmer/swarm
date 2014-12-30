package swarm.client.app;

import swarm.client.entities.A_ClientUser;
import swarm.client.entities.ClientGrid;
import swarm.client.view.ViewController;
import swarm.client.view.cell.SpritePlateAnimation;
import swarm.shared.app.AppConfig;
import swarm.shared.entities.A_Grid;
import swarm.shared.structs.Point;

public class ClientAppConfig extends AppConfig
{
//	public double killQueueTime = 1.0;
	public double killQueueTime = 5.0;

	public double timeThatMetaCellSticksAroundAfterCameraStopsZooming = .18;
//	public double timeThatMetaCellSticksAroundAfterCameraStopsZooming = 10.0;
	
	
	
	public double floatingHistoryUpdateFreq_seconds = .75;
	public double cellHudHeight;
	public int metaLevelCount = 1;
	public double minSnapTime = .8;
	public double snapTimeRange = 1;
	public int framerate_milliseconds = 33;
	public double backOffDistance;
	
	
	public boolean makeGridRequest = true;
	
	public boolean useVirtualSandbox = true;
	
	public int addressCacheSize = 1024;
	public double addressCacheExpiration_seconds = 60 * 5;
	
	public int cellSizeCacheSize = 1024;
	public double cellSizeCacheExpiration_seconds = 60 * 5;
	
	public int codeCacheSize = 128;
	public double codeCacheExpiration_seconds = 60 * 5;
	
	public Point startingPoint = null;
	
	public A_ClientUser user;
	public ClientGrid grid;
	public ViewController viewController;
	
	public int maxSubCellDimension = 1;
}
