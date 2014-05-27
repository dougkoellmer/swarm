package swarm.client.navigation;

import swarm.shared.lang.Boolean;
import swarm.shared.structs.CellSize;
import swarm.shared.structs.Rect;
import swarm.shared.structs.Vector;

public class FocusedLayout
{
	public Vector topLeftOffset = new Vector();
	public Rect window = new Rect();
	public Rect cellSizePlusExtras = new Rect();
	public CellSize cellSize = new CellSize();
	
	Boolean widthSmaller = new Boolean();
	Boolean heightSmaller = new Boolean();
}
