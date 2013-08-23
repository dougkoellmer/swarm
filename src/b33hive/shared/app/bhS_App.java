package b33hive.shared.app;

import b33hive.shared.utils.bhU_BitTricks;

public class bhS_App
{
	public static final int SERVER_VERSION = 9;
	
	public static final int UNKNOWN_SERVER_VERSION = -1;
	
	public static final double DEPTH_OF_FIELD = 500;
	
	public static final String CELL_ADDRESS_REGEX = "^[a-zA-Z0-9_]*$";
	
	public static final double MAX_ZOOM_CELL_PIXEL_COUNT = 1;
	public static final double MIN_Z = 0;
	public static final double MIN_MAX_Z = 300;
	
	public static final int MAX_IMAGED_CELL_SIZE = 128;
	
	public static final int MAX_CELL_IMAGES = bhU_BitTricks.calcBitPosition(MAX_IMAGED_CELL_SIZE);
	
	public static final int MAX_CELL_ADDRESS_PARTS = 12; //TODO: arbitrary...
}
