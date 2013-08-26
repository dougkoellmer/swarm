package swarm.client.ui.cell;

import swarm.client.app.sm_c;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smCamera;
import swarm.client.managers.smCellBuffer;
import swarm.client.managers.smCellBufferManager;
import swarm.client.navigation.smMouseNavigator;
import swarm.client.input.smMouse;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.ui.smE_ZIndex;
import swarm.client.ui.smI_UIElement;
import swarm.client.ui.smS_UI;
import swarm.client.ui.smU_UI;
import swarm.client.ui.sm_view;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.utils.smU_Math;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class smVisualCellHighlight extends FlowPanel implements smI_UIElement
{	
	private final smPoint m_utilPoint1 = new smPoint();
	
	private double m_lastScaling = -1;
	
	public smVisualCellHighlight(Panel parent)
	{
		this.addStyleName("cell_highlight");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		smE_ZIndex.CELL_HIGHLIGHT.assignTo(this);
		
		this.setVisible(false);
		
		parent.add(this);
	}
	
	private void update()
	{
		smMouseNavigator navManager = smMouseNavigator.getInstance();
		boolean isMouseTouchingSnappableCell = navManager.isMouseTouchingSnappableCell();
		
		//--- DRK > TODO: Really minor so might never fix, but this is kinda sloppy.
		//---				There should probably be a "panning" state or something that the highlight listens for instead.
		smMouse mouse = navManager.getMouse();
		boolean isPanning = mouse.isMouseDown() && mouse.hasMouseStrayedWhileDown();
		
		if( !isMouseTouchingSnappableCell || isPanning )
		{
			this.setVisible(false);
			
			return;
		}
		
		smCellBuffer buffer = smCellBufferManager.getInstance().getDisplayBuffer();
		int subCellDim = buffer.getSubCellCount();
		
		smCamera camera = sm_c.camera;
		smPoint basePoint = null;
		double highlightScaling = camera.calcDistanceRatio();
		
		smGridCoordinate mouseCoord = navManager.getMouseGridCoord();
		smBufferCell cell = buffer.getCellAtAbsoluteCoord(mouseCoord);
		
		if( cell == null )
		{
			smU_Debug.ASSERT(false, "Expected cell to be non null.");
			
			return;
		}
		
		basePoint = m_utilPoint1;
		
		//--- DRK > For this case, we have to do all kinds of evil witch hackery to ensure that cell highlight lines up
		//---		visually with individual cells in the meta cell images...this technically creates some disagreement
		//---		between the highlight and the actual mouse coordinate position for near-cell-boundary cases, but it's zoomed 
		//---		out enough that it doesn't really matter...you'd really have to look for it to notice a discrepancy.
		
		smVisualCellManager cellManager = sm_view.cellMngr;
		double lastScaling = cellManager.getLastScaling();
		smPoint lastBasePoint = cellManager.getLastBasePoint();
		
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
			
			int shadowSize = (int) (((double)smS_UI.HIGHLIGHT_MAX_SIZE) * (scale));
			shadowSize = shadowSize < smS_UI.HIGHLIGHT_MIN_SIZE ? smS_UI.HIGHLIGHT_MIN_SIZE : shadowSize;
			
			smU_UI.setBoxShadow(this.getElement(), "0 0 "+shadowSize+"px " + smS_UI.HIGHLIGHT_COLOR);
		}
		
		m_lastScaling = highlightScaling;
		
		this.setVisible(true);
	}

	public void onStateEvent(smStateEvent event)
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
