package swarm.client.view.cell;

import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.Camera;
import swarm.client.entities.ClientGrid;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.navigation.MouseNavigator;
import swarm.client.navigation.U_CameraViewport;
import swarm.client.input.Mouse;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.view.E_ZIndex;
import swarm.client.view.I_UIElement;
import swarm.client.view.S_UI;
import swarm.client.view.U_Css;
import swarm.client.view.ViewConfig;
import swarm.client.view.ViewContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.utils.U_Math;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class VisualCellHighlight extends FlowPanel implements I_UIElement
{
	private static final Logger s_logger = Logger.getLogger(VisualCellHighlight.class.getName());
	private final Point m_utilPoint1 = new Point();
	
	private double m_lastScaling = -1;
	
	private final ViewContext m_viewContext;
	
	public VisualCellHighlight(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		this.addStyleName("cell_highlight");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		E_ZIndex.CELL_HIGHLIGHT.assignTo(this);
		
		this.setVisible(false);
	}
	
	private void update()
	{
		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
		{
			this.setVisible(false);
			return;
		}
		
		MouseNavigator navManager = m_viewContext.mouseNavigator;
		boolean isMouseTouchingSnappableCell = navManager.isMouseTouchingSnappableCell();
		
		//--- DRK > TODO: Really minor so might never fix, but this is kinda sloppy.
		//---				There should probably be a "panning" state or something that the highlight listens for instead.
		Mouse mouse = navManager.getMouse();
		boolean isPanning = mouse.isMouseDown() && mouse.hasMouseStrayedWhileDown();
		
		if( isPanning )
		{
			this.setVisible(false);
			
			return;
		}
		
		CellBuffer buffer = m_viewContext.appContext.cellBufferMngr.getBaseDisplayBuffer();
		int subCellDim = m_viewContext.appContext.cellBufferMngr.getSubCellCount();
		
		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid();
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		Point basePoint = null;
		double highlightScaling = camera.calcDistanceRatio();
		
		GridCoordinate mouseCoord = navManager.getMouseGridCoord();
		boolean owned = grid.isTaken(mouseCoord, 1);
		
		BufferCell cell = buffer.getCellAtAbsoluteCoord(mouseCoord);
		
		if( !owned )
		{
			this.setVisible(false);
			
			return;
		}

		State_CameraSnapping snappingState = m_viewContext.stateContext.getEntered(State_CameraSnapping.class);
		if( snappingState != null )
		{
			if( mouseCoord.isEqualTo(snappingState.getTargetCoord()) )
			{
				this.setVisible(false);
				
				return;
			}
		}
		
		basePoint = m_utilPoint1;
		
		//--- DRK > For this case, we have to do all kinds of evil witch hackery to ensure that cell highlight lines up
		//---		visually with individual cells in the meta cell images...this technically creates some disagreement
		//---		between the highlight and the actual mouse coordinate position for near-cell-boundary cases, but it's zoomed 
		//---		out enough that it doesn't really matter...you'd really have to look for it to notice a discrepancy.
		
		VisualCellManager cellManager = m_viewContext.cellMngr;
		VisualCell visualCell = (VisualCell) (cell != null ? cell.getVisualization() : null);
		double lastScaling = cellManager.getLastScaling();
		Point lastBasePoint = cellManager.getLastBasePoint();
		
//		s_logger.severe(""+lastBasePoint + " " + lastScaling);
		
		int bufferM = buffer.getCoordinate().getM();//buffer.getCoordinate().getM() * subCellDim;
		int bufferN = buffer.getCoordinate().getN();//buffer.getCoordinate().getN() * subCellDim;
		int deltaM = mouseCoord.getM() - bufferM;
		int deltaN = mouseCoord.getN() - bufferN;
		
		//TODO: Assuming square cell size.
		double apparentCellPixelsX = 0, apparentCellPixelsY = 0;
		
		int defaultCellWidth = grid.getCellWidth();
		int defaultCellHeight = grid.getCellHeight();
		int visualCellWidth = visualCell != null ? visualCell.getWidth() : defaultCellWidth;
		int visualCellHeight = visualCell != null ? visualCell.getHeight() : defaultCellHeight;
		
//		if( subCellDim > 1 )
//		{
//			apparentCellPixelsX = (((defaultCellWidth+ grid.getCellPadding()) / ((double) subCellDim)) * lastScaling);
//			apparentCellPixelsY = (((defaultCellHeight+ grid.getCellPadding()) / ((double) subCellDim)) * lastScaling);
//		}
//		else
		{
			apparentCellPixelsX = (defaultCellWidth + grid.getCellPadding()) * lastScaling;
			apparentCellPixelsY = (defaultCellHeight + grid.getCellPadding()) * lastScaling;
		}
		
		double deltaPixelsX = apparentCellPixelsX * deltaM;
		double deltaPixelsY = apparentCellPixelsY * deltaN;

		basePoint.copy(lastBasePoint);
		basePoint.inc(deltaPixelsX, deltaPixelsY, 0);
		basePoint.incX((visualCell != null ? (double)visualCell.getXOffset() : 0.0)*lastScaling);
		basePoint.incY((visualCell != null ? (double)visualCell.getYOffset() : 0.0)*lastScaling);
		double y = basePoint.getY();
		
		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
		{
			Element scrollElement = this.getParent().getParent().getElement();
			y += scrollElement.getScrollTop();
		}
		
		String width = (visualCellWidth * highlightScaling) + "px";
		String height = (visualCellHeight * highlightScaling) + "px";
		this.setSize(width, height);
		this.getElement().getStyle().setProperty("top", y + "px");
		this.getElement().getStyle().setProperty("left", basePoint.getX() + "px");
		
//		s_logger.severe(y + " " + basePoint.getX());
		
		ViewConfig viewConfig = m_viewContext.config;
		
		if( m_lastScaling != highlightScaling )
		{
			double scale =  Math.sqrt(highlightScaling);
			
			int shadowSize = (int) (((double)viewConfig.cellHighlightMaxSize) * (scale));
			shadowSize = (shadowSize < viewConfig.cellHighlightMinSize ? viewConfig.cellHighlightMinSize : shadowSize);
			
			U_Css.setBoxShadow(this.getElement(), "0 0 "+(shadowSize/2)+"px "+(shadowSize/2)+"px " + m_viewContext.config.cellHighlightColor);
		}
		
		m_lastScaling = highlightScaling;
		
		this.setVisible(true);
	}

	public void onStateEvent(StateEvent event)
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
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SetViewSize.class ||
					event.getAction() == Action_Camera_SnapToPoint.class )
				{
					State_ViewingCell state = event.getContext().getEntered(State_ViewingCell.class);
					
					if( state != null )
					{
						this.update();
					}
				}
				
				break;
			}
		}
	}
}
