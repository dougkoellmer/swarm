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
import swarm.client.view.cell.VisualCell.E_MetaState;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.U_Grid;
import swarm.shared.utils.U_Bits;
import swarm.shared.utils.U_Math;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_BaseStateEvent;
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
	
	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured()
	{
		@Override public boolean isVisualizationLoaded()
		{
			CellBufferManager bufferMngr = m_viewContext.appContext.cellBufferMngr;
			CellBuffer buffer = bufferMngr.getDisplayBuffer(U_Bits.calcBitPosition(this.subCellCount));
			BufferCell cell = buffer.getCellAtAbsoluteCoord(this.m, this.n);
			
			if( cell != null )
			{
				VisualCell visualCell = (VisualCell) cell.getVisualization();
				if( visualCell.getMetaState() == E_MetaState.NOT_SET )
				{
					return !visualCell.isMetaImageProbablyInMemory();
				}
				else
				{
					return true;
				}
			}
			
			return false;
		}
	};
	
	public VisualCellHighlight(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		this.addStyleName("cell_highlight");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		E_ZIndex.CELL_HIGHLIGHT.assignTo(this);
		
//		this.getElement().getStyle().setBackgroundColor(m_viewContext.config.cellHighlightColor);
		
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
		
		//--- DRK > TODO: Really minor so might never fix, but this is kinda sloppy.
		//---				There should probably be a "panning" state or something that the highlight listens for instead.
		Mouse mouse = navManager.getMouse();
		boolean isPanning = mouse.isMouseDown() && mouse.hasMouseStrayedWhileDown();
		
		if( isPanning )
		{
			this.setVisible(false);
			
			return;
		}
		
		CellBuffer buffer_lowest = m_viewContext.appContext.cellBufferMngr.getLowestDisplayBuffer();
		int subCellCount_highest = m_viewContext.appContext.cellBufferMngr.getSubCellCount();
		
		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid();
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		Point basePoint = null;
		
		GridCoordinate mouseCoord = navManager.getMouseGridCoord();
		boolean owned = grid.isTaken(mouseCoord, 1);
		
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
		
		CellBuffer buffer_highest = m_viewContext.appContext.cellBufferMngr.getHighestDisplayBuffer();
		
		BufferCell cell = buffer_lowest.getCellAtAbsoluteCoord(mouseCoord);
		
		int subCellCount = m_viewContext.appContext.cellBufferMngr.getSubCellCount();
		
		if( subCellCount > 1 )
		{
			if( cell == null )//|| !cell_1.getVisualization().isLoaded() )
			{
				if( !grid.isObscured(mouseCoord.getM(), mouseCoord.getN(), 1, buffer_highest.getSubCellCount(), m_obscured) )
				{
					this.setVisible(false);
					
					return;
				}
			}
		}
		
		basePoint = m_utilPoint1;
		
		//--- DRK > For this case, we have to do all kinds of evil witch hackery to ensure that cell highlight lines up
		//---		visually with individual cells in the meta cell images...this technically creates some disagreement
		//---		between the highlight and the actual mouse coordinate position for near-cell-boundary cases, but it's zoomed 
		//---		out enough that it doesn't really matter...you'd really have to look for it to notice a discrepancy.
		
		VisualCellManager cellManager = m_viewContext.cellMngr;
		VisualCell visualCell = (VisualCell) (cell != null ? cell.getVisualization() : null);
		double sizeScaling = cellManager.getLastScaling();
		double distanceRatio = camera.calcDistanceRatio();
		Point lastBasePoint = cellManager.getLastBasePoint();
		
//		s_logger.severe(""+lastBasePoint + " " + lastScaling);
		
		int bufferM = buffer_highest.getCoordinate().getM() * subCellCount_highest;
		int bufferN = buffer_highest.getCoordinate().getN() * subCellCount_highest;
		int deltaM = mouseCoord.getM() - bufferM;
		int deltaN = mouseCoord.getN() - bufferN;
		
		double lastMetaCellWidth = cellManager.getLastMetaCellWidth();
		double lastMetaCellHeight = cellManager.getLastMetaCellHeight();
		int deltaM_mod = deltaM % subCellCount_highest;
		int deltaN_mod = deltaN % subCellCount_highest;
		deltaM -= deltaM_mod;
		deltaN -= deltaN_mod;
		deltaM /= subCellCount_highest;
		deltaN /= subCellCount_highest;
		
		int defaultCellWidth = grid.getCellWidth();
		int defaultCellHeight = grid.getCellHeight();
		
		double deltaPixelsX = deltaM * lastMetaCellWidth + deltaM_mod*(((double)lastMetaCellWidth)/subCellCount_highest);
		double deltaPixelsY = deltaN * lastMetaCellHeight + deltaN_mod*(((double)lastMetaCellHeight)/subCellCount_highest);

		basePoint.copy(lastBasePoint);
		basePoint.inc(deltaPixelsX, deltaPixelsY, 0);
		basePoint.incX((visualCell != null ? (double)visualCell.getXOffset() : 0.0)*sizeScaling);
		basePoint.incY((visualCell != null ? (double)visualCell.getYOffset() : 0.0)*sizeScaling);
		double y = basePoint.getY();
		
		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
		{
			Element scrollElement = this.getParent().getParent().getElement();
			y += scrollElement.getScrollTop();
		}
		
		double pinch = m_viewContext.config.canvasBackingPinch;
		double visualCellWidth = visualCell != null ? visualCell.getWidth() : defaultCellWidth;
		double visualCellHeight = visualCell != null ? visualCell.getHeight() : defaultCellHeight;
		
		visualCellWidth *= distanceRatio;
		visualCellHeight *= distanceRatio;
		visualCellWidth -= pinch*2;
		visualCellHeight -= pinch*2;
		basePoint.incX(pinch);
		y += pinch;
		String width = (visualCellWidth) + "px";
		String height = (visualCellHeight) + "px";
		this.setSize(width, height);
		this.getElement().getStyle().setProperty("top", y + "px");
		this.getElement().getStyle().setProperty("left", basePoint.getX() + "px");
		
//		s_logger.severe(y + " " + basePoint.getX());
		
		ViewConfig viewConfig = m_viewContext.config;
		
		if( m_lastScaling != distanceRatio )
		{
			double scale =  Math.sqrt(distanceRatio);
			
			int shadowSize = (int) (((double)viewConfig.cellHighlightMaxSize) * (scale));
			shadowSize = (shadowSize < viewConfig.cellHighlightMinSize ? viewConfig.cellHighlightMinSize : shadowSize);
			
			U_Css.setBoxShadow(this.getElement(), "0 0 "+(shadowSize/2)+"px "+(shadowSize/2)+"px " + m_viewContext.config.cellHighlightColor);
		}
		
		m_lastScaling = distanceRatio;
		
		this.setVisible(true);
	}

	public void onStateEvent(A_BaseStateEvent event)
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
				if( event.getTargetClass() == Action_Camera_SetViewSize.class ||
					event.getTargetClass() == Action_Camera_SnapToPoint.class )
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
