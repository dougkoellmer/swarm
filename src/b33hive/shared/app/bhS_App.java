package b33hive.shared.app;

import b33hive.shared.utils.bhU_BitTricks;

public class bhS_App
{
	public static final boolean VERBOSE_TRANSACTIONS = false; //TODO: Move to app config
	
	public static final int SERVER_VERSION = 8;
	
	public static final int UNKNOWN_SERVER_VERSION = -1;
	
	
	//public static final double CELL_PIXEL_COUNT = 512;
	//public static final double CELL_SPACING_PIXEL_COUNT = 16;
	public static final double DEPTH_OF_FIELD = 500;
	//public static final double CELL_SPACING_RATIO = CELL_SPACING_PIXEL_COUNT / CELL_PIXEL_COUNT;
	
	//public static final double CELL_PLUS_SPACING_PIXEL_COUNT = CELL_PIXEL_COUNT + CELL_SPACING_PIXEL_COUNT;
	
	//public static final double SCALING_RATIO = CELL_PLUS_SPACING_PIXEL_COUNT / CELL_PIXEL_COUNT;
	
	public static final double MAX_ZOOM_CELL_PIXEL_COUNT = 1;
	public static final double MIN_Z = 0;
	public static final double MIN_MAX_Z = 300;
	
	public static final int MAX_IMAGED_CELL_SIZE = 128;
	
	public static final int MAX_CELL_IMAGES = bhU_BitTricks.calcBitPosition(MAX_IMAGED_CELL_SIZE);
	
	public static final int MAX_CELL_ADDRESS_PARTS = 2;
	
	public static final int SANDBOX_COORD_M = 2;
	public static final int SANDBOX_COORD_N = 2;
}
