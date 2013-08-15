package b33hive.client.ui.cell;

import java.util.logging.Logger;


import b33hive.client.ui.*;
import b33hive.client.ui.alignment.bhAlignmentDefinition;
import b33hive.client.ui.alignment.bhAlignmentRect;
import b33hive.client.ui.alignment.bhE_AlignmentPosition;
import b33hive.client.ui.alignment.bhE_AlignmentType;
import b33hive.client.ui.tooltip.bhE_ToolTipMood;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTip;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhMagnifier;
import b33hive.client.states.*;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_GettingMapping;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.app.bhClientAppConfig;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhCamera;
import b33hive.client.managers.bhCellBufferManager;
import b33hive.client.navigation.bhMouseNavigator;
import b33hive.shared.app.bhS_App;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class bhVisualCellContainer extends FlowPanel implements ResizeHandler, bhI_UIElement
{
	private static final Logger s_logger = Logger.getLogger(bhVisualCellContainer.class.getName());
	
	private static final bhPoint s_utilPoint1 = new bhPoint();
	private static final bhPoint s_utilPoint2 = new bhPoint();
	
	private final bhMagnifier m_magnifier;
	
	private final FlowPanel m_cellContainerInner = new FlowPanel();
	private final FlowPanel m_splashGlass = new FlowPanel();
	
	private final bhVisualCellCropper[] m_cropper = new bhVisualCellCropper[2];
	
	private final bhAlignmentDefinition m_statusAlignment = new bhAlignmentDefinition();
	
	private boolean m_showingMappingNotFound = false;
	
	private final bhToolTipConfig m_gettingAddressTipConfig = new bhToolTipConfig(bhE_ToolTipType.STATUS, m_statusAlignment, "Resolving address...");
	private final bhToolTipConfig m_mappingNotFoundTipConfig = new bhToolTipConfig(bhE_ToolTipType.NOTIFICATION, m_statusAlignment, "Address not found!", bhE_ToolTipMood.OOPS);
	
	private final StateMachine_Camera.SetCameraViewSize.Args m_args_SetCameraViewSize = new StateMachine_Camera.SetCameraViewSize.Args();
	
	public bhVisualCellContainer(bhViewConfig config)
	{
		m_magnifier = new bhMagnifier(config.magnifierTickCount, config.magFadeInTime_seconds);
		
		m_splashGlass.addStyleName("bh_splash_glass");
		m_cellContainerInner.addStyleName("bh_cell_container_inner");
		this.addStyleName("bh_cell_container");
		
		bhE_ZIndex.CELL_SPLASH_GLASS.assignTo(m_splashGlass);
		
		Window.addResizeHandler(this);

		//bhU_UI.toggleSelectability(this.getElement(), false);
		
		final LayoutPanel magnifierContainer = new LayoutPanel();
		magnifierContainer.setSize("100%", "100%");
		magnifierContainer.add(m_magnifier);
		magnifierContainer.setWidgetRightWidth(m_magnifier, 8, Unit.PX, 34, Unit.PX);
		magnifierContainer.setWidgetTopHeight(m_magnifier, 8, Unit.PX, 196, Unit.PX);
		
		for( int i = 0; i < m_cropper.length; i++ )
		{
			m_cropper[i] = new bhVisualCellCropper();
			m_cellContainerInner.add(m_cropper[i]);
		}
		
		this.add(magnifierContainer);
		this.add(m_cellContainerInner);
		this.add(m_splashGlass);

		m_statusAlignment.setPosition(bhE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, bhE_AlignmentPosition.RIGHT_OR_BOTTOM);
		m_statusAlignment.setPosition(bhE_AlignmentType.MASTER_ANCHOR_VERTICAL, bhE_AlignmentPosition.RIGHT_OR_BOTTOM);
		m_statusAlignment.setPosition(bhE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, bhE_AlignmentPosition.RIGHT_OR_BOTTOM);
		m_statusAlignment.setPosition(bhE_AlignmentType.SLAVE_ANCHOR_VERTICAL, bhE_AlignmentPosition.RIGHT_OR_BOTTOM);
		m_statusAlignment.setPadding(bhE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, bhS_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
		m_statusAlignment.setPadding(bhE_AlignmentType.SLAVE_ANCHOR_VERTICAL, bhS_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
	}
	
	public FlowPanel getMouseEnabledLayer()
	{
		return m_splashGlass;
	}
	
	public FlowPanel getCellContainerInner()
	{
		return m_cellContainerInner;
	}
	
	public bhMagnifier getMagnifier()
	{
		return m_magnifier;
	}
	
	private void hideCroppers()
	{
		for( int i = 0; i < m_cropper.length; i++ )
		{
			m_cropper[i].setVisible(false);
		}
	}
	
	private void updateCroppers()
	{
		//--- DRK > If cell sub count is 1, it means we can match the bottom/right sides of the grid exactly.
		int cellSubCount = bhCellBufferManager.getInstance().getDisplayBuffer().getSubCellCount();
		if( cellSubCount == 0 || cellSubCount == 1 )
		{
			hideCroppers();
			
			return;
		}

		//--- DRK > If the mod operation is 0, we can also always match bottom/right sides exactly.
		bhA_Grid grid = bh_c.gridMngr.getGrid();
		if( (grid.getWidth() % cellSubCount) == 0 && (grid.getHeight() % cellSubCount) == 0 )
		{
			hideCroppers();
			
			return;
		}
		
		bhCamera camera = bh_c.camera;
	
		double gridWidthInPixels = grid.calcPixelWidth();
		double gridHeightInPixels = grid.calcPixelHeight();
		
		bhPoint worldPoint = s_utilPoint1;
		bhPoint screenPoint = s_utilPoint2;

		worldPoint.zeroOut();
		worldPoint.inc(gridWidthInPixels, gridHeightInPixels, 0);
		camera.calcScreenPoint(worldPoint, screenPoint);
		double[] screenDimensions = {this.getOffsetWidth(), this.getOffsetHeight()};
		double scaling = camera.calcDistanceRatio();
		
		//--- DRK > If cell size is greater than 1, and grid isn't power of two, we have to bring
		//---		the croppers into play if we can see the bottom or right sides of the grid.
		for( int i = 0; i < m_cropper.length; i++ )
		{
			double screenComponent = screenPoint.getComponent(i);
			
			if( screenComponent <= screenDimensions[i] )
			{
				m_cropper[i].setVisible(true);
				
				double component = screenComponent < 0 ? 0 : screenComponent;
				component += scaling * grid.getCellPadding();
				component -= 1; // just to make sure we don't have a pixel sliver of the next cell over.
				
				m_cropper[i].setPositionComponent(i, component);
			}
			else
			{
				m_cropper[i].setVisible(false);
			}
		}
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType() )
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof StateMachine_Camera )
				{
					//--- DRK > For some reason we can't use the updateCameraViewRect method because
					//---		offset width/height return 0...I think it's because CSS isn't applied yet.
					//---		So kind of a hack here...would be nice if I could fix this, so...TODO(DRK).
					double width = bh_view.splitPanel.getCellPanelWidth();
					double x = bh_view.splitPanel.getTabPanelWidth();
					double height = RootPanel.get().getOffsetHeight();
					
					m_args_SetCameraViewSize.set(width, height);
					
					bhA_Action.perform(StateMachine_Camera.SetCameraViewSize.class, m_args_SetCameraViewSize);
					
					//--- DRK > Because width/height of "this" is still 0/0, we temporarily
					//---		give the tool tip an override rectangle to work with.
					m_statusAlignment.setMasterRect(new bhAlignmentRect(x, 0, width, height));
					
					//--- DRK > This used to be uncommented, but new set up means width/height of this container is still 0 at this point.
					//updateCameraViewRect();
				}
				else if ( event.getState() instanceof State_ViewingCell )
				{
					//TODO: Make sure cell exits cleanly, somehow.
					
					//bhVisualCell viewedVisualCell = (bhVisualCell) m_viewedCell.getVisualization();
					
					//bhU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
					
					//viewedVisualCell.getElement().blur(); // TODO: This doesn't work to clear selection of cell contents if you navigate away.
					
					//m_viewedCell = null;
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					this.updateCroppers();
				}
				
				if( event.getState() instanceof State_ViewingCell )
				{
					//--- DRK > Note that the above update could have caused the machine to exit this state.
					if( event.getState()./*still*/isEntered() )
					{
						bhBufferCell viewedCell = ((State_ViewingCell) event.getState()).getCell();
						
						bhMouseNavigator navigator = bhMouseNavigator.getInstance();
	
						/*if( navigator.getMouseGridCoord().isEqualTo(viewedCell.getCoordinate()) )
						{
							bhU_UI.toggleSelectability(m_cellContainerInner.getElement(), true);
						}
						else
						{
							bhU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
						}*/
					}
					else
					{
						//bhU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
					}
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_GettingMapping )
				{
					m_showingMappingNotFound = false;
					
					bh_c.toolTipMngr.removeTip(this);
					bh_c.toolTipMngr.addTip(this, m_gettingAddressTipConfig);
				}
				
				break;
			}
			
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof State_GettingMapping )
				{
					if( !m_showingMappingNotFound )
					{
						bh_c.toolTipMngr.removeTip(this);
					}
					
					m_showingMappingNotFound = false;
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == State_GettingMapping.OnResponse.class )
				{
					State_GettingMapping.OnResponse.Args args = event.getActionArgs();
					
					if( args.getType() != State_GettingMapping.OnResponse.E_Type.ON_FOUND )
					{
						bh_c.toolTipMngr.removeTip(this);
						bh_c.toolTipMngr.addTip(this, m_mappingNotFoundTipConfig);
						
						m_showingMappingNotFound = true;
					}
				}
				
				break;
			}
		}
		
		m_magnifier.onStateEvent(event);
	}
	
	private void updateCameraViewRect()
	{
		m_args_SetCameraViewSize.set((double)this.getOffsetWidth(), (double)this.getOffsetHeight());
		bhA_Action.perform(StateMachine_Camera.SetCameraViewSize.class, m_args_SetCameraViewSize);
	}
	
	public void onResize()
	{
		updateCameraViewRect();
		
		this.updateCroppers();
		
		m_statusAlignment.setMasterRect(null);
		
		bh_c.toolTipMngr.onTipMove(this);
	}

	@Override
	public void onResize(ResizeEvent event)
	{
		this.onResize();
	}
}
