package swarm.client.managers;

public class F_BufferUpdateOption
{
	public static final int NONE					= 0x0;
	public static final int CREATE_VISUALIZATIONS	= 0x1;
	public static final int COMMUNICATE_WITH_SERVER	= 0x2;
	public static final int FLUSH_CELL_POPULATOR	= 0x4;
	public static final int JUST_REMOVED_OVERRIDE	= 0x8;
	public static final int ALL_DEFAULTS			= 0xFFFFFFFF & ~JUST_REMOVED_OVERRIDE;
}