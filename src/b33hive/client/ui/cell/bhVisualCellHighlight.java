package b33hive.client.ui.cell;

import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhCamera;
import b33hive.client.managers.bhCellBuffer;
import b33hive.client.managers.bhCellBufferManager;
import b33hive.client.navigation.bhMouseNavigator;
import b33hive.client.input.bhMouse;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.ui.bhE_ZIndex;
import b33hive.client.ui.bhI_UIElement;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.bhU_UI;
import b33hive.client.ui.bh_view;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.utils.bhU_Math;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class bhVisualCellHighlight extends FlowPanel implements bhI_UIElement
{	
	private final bhPoint m_utilPoint1 = new bhPoint();
	
	private double m_lastScaling = -1;
	
	public bhVisualCellHighlight(Panel parent)
	{
		this.addStyleName("cell_highlight");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		bhE_ZIndex.CELL_HIGHLIGHT.assignTo(this);
		
		this.setVisible(false);
		
		parent.add(this);
	}
	
	private void update()
	{
		bhMouseNavigator navManager = bhMouseNavigator.getInstance();
		boolean isMouseTouchingSnappableCell = navManager.isMouseTouchingSnappableCell();
		
		//--- DRK > TODO: Really minor so might never fix, but this is kinda sloppy.
		//---				There should probably be a "panning" state or something that the highlight listens for instead.
		bhMouse mouse = navManager.getMouse();
		boolean isPanning = mouse.isMouseDown() && mouse.hasMouseStrayedWhileDown();
		
		if( !isMouseTouchingSnappableCell || isPanning )
		{
			this.setVisible(false);
			
			return;
		}
		
		bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
		int subCellDim = buffer.getSubCellCount();
		
		bhCamera camera = bh_c.camera;
		bhPoint basePoint = null;
		double highlightScaling = camera.calcDistanceRatio();
		
		bhGridCoordinate mouseCoord = navManager.getMouseGridCoord();
		bhBufferCell cell = buffer.getCellAtAbsoluteCoord(mouseCoord);
		
		if( cell == null )
		{
			bhU_Debug.ASSERT(false, "Expected cell to be non null.");
			
			return;
		}
		
		basePoint = m_utilPoint1;
		
		//--- DRK > For this case, we have to do all kinds of evil witch hackery to ensure that cell highlight lines up
		//---		visually with individual cells in the meta cell images...this technically creates some disagreement
		//---		between the highlight and the actual mouse coordinate position for near-cell-boundary cases, but it's zoomed 
		//---		out enough that it doesn't really matter...you'd really have to look for it to notice a discrepancy.
		
		bhVisualCellManager cellManager = bh_view.cellMngr;
		double lastScaling = cellManager.getLastScaling();
		bhPoint lastBasePoint = cellManager.getLastBasePoint();
		
		int bufferM = buffer.getCoordinate().getM() * subCellDim;
		int bufferN = buffer.getCoordinate().getN() * subCellDim;
		int deltaM = mouseCoord.getM() - bufferM;
		int deltaN = mouseCoord.getN() - bufferN;
		
		//TODO: Assuming square cell size.
		double apparentCellPixels = 0;
		
		if( buffer.getSubCellCount() > 1 )
		{
			apparentCellPixels = ((cell.getGrid().getCellWidth() / ((double) subCellDim)) * lastScaling);
		}
		else
		{
			apparentCellPixels = (cell.getGrid().getCellWidth() + cell.getGrid().getCellPadding()) * lastScaling;
		}
		
		double deltaPixelsX = apparentCellPixels * deltaM;
		double deltaPixelsY = apparentCellPixels * deltaN;

		basePoint.copy(lastBasePoint);
		basePoint.inc(deltaPixelsX, deltaPixelsY, 0);
		
		String size = (cell.getGrid().getCellWidth() * highlightScaling) + "px";
		this.setSize(size, size);
		this.getElement().getStyle().setProperty("top", basePoint.getY() + "px");
		this.getElement().getStyle().setProperty("left", basePoint.getX() + "px");
		
		if( m_lastScaling != highlightScaling )
		{
			double scale =  Math.sqrt(highlightScaling);
			
			int shadowSize = (int) (((double)bhS_UI.HIGHLIGHT_MAX_SIZE) * (scale));
			shadowSize = shadowSize < bhS_UI.HIGHLIGHT_MIN_SIZE ? bhS_UI.HIGHLIGHT_MIN_SIZE : shadowSize;
			
			bhU_UI.setBoxShadow(this.getElement(), "0 0 "+shadowSize+"px " + bhS_UI.HIGHLIGHT_COLOR);
		}
		
		m_lastScaling = highlightScaling;
		
		this.setVisible(true);
	}

	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					this.update();
				}
				
				break;
			}
		}
	}
}
