package com.b33hive.client.navigation;

import java.util.logging.Logger;

import com.b33hive.client.entities.bhCamera;
import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.input.bhBrowserAddressManager;
import com.b33hive.client.input.bhMouse;
import com.b33hive.client.input.bhMouseEvent;
import com.b33hive.client.managers.bhCameraManager;
import com.b33hive.client.states.StateContainer_Base;
import com.b33hive.client.states.camera.StateMachine_Camera;
import com.b33hive.client.states.camera.State_CameraFloating;
import com.b33hive.client.states.camera.State_CameraSnapping;
import com.b33hive.client.states.camera.State_ViewingCell;
import com.b33hive.client.ui.bhI_UIElement;
import com.b33hive.client.ui.bhS_UI;
import com.b33hive.client.ui.bhU_UI;
import com.b33hive.client.ui.cell.bhVisualCell;
import com.b33hive.shared.bhU_Math;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhE_GetCellAddressMappingError;
import com.b33hive.shared.structs.bhGetCellAddressMappingResult;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.structs.bhPoint;
import com.b33hive.shared.structs.bhTolerance;
import com.b33hive.shared.structs.bhVector;
import com.b33hive.shared.structs.bhVelocitySmoother;
import com.google.gwt.user.client.ui.Panel;

/**
 * ...
 * @author
 */
public class bhMouseNavigator implements bhI_UIElement, bhMouse.I_Listener
{
	private static final Logger s_logger = Logger.getLogger(bhMouseNavigator.class.getName());
	
	private static final double SCROLL_CAP = 10;
	private static final double BASE_SCROLL_SCALE = 10;
	private static final double DISTANCE_SCROLL_SCALE = 10;
	private static final bhTolerance MOUSE_TOLERANCE = new bhTolerance(bhTolerance.DEFAULT);
	
	private static bhMouseNavigator s_instance = null;
	
	private final bhPoint m_mouseZoomPoint2d = new bhPoint();
	private final bhPoint m_mouseZoomPoint3d = new bhPoint();
	private boolean m_isZooming = false;
	
	private StateMachine_Camera m_cameraController = null;
	private bhA_State m_cameraState = null;
	
	private final bhPoint m_utilPoint1 = new bhPoint();
	private final bhPoint m_utilPoint2 = new bhPoint();
	private final bhVector m_utilVector = new bhVector();
	
	private final bhPoint m_grabPoint = new bhPoint();
	private final bhPoint m_lastWorldPoint = new bhPoint();
	private final bhVelocitySmoother m_flickSmoother = new bhVelocitySmoother(bhS_UI.FLICK_SMOOTHING_SAMPLE_COUNT);
	
	
	private final StateMachine_Camera.SetCameraTarget.Args m_setTargetArgs = new StateMachine_Camera.SetCameraTarget.Args();
	
	private final bhGridCoordinate m_mouseGridCoord = new bhGridCoordinate();
	private boolean m_isMouseTouchingSnappableCell = false;
	
	private final bhMouse m_mouse;
	private boolean m_mouseWentDownOnViewedCell = false;
	
	bhMouseNavigator(bhMouse mouse)
	{
		m_mouse = mouse;
		
		m_mouse.setListener(this);
		
		s_instance = this;
	}
	
	public static bhMouseNavigator getInstance()
	{
		if( s_instance == null )
		{
			bhU_Debug.ASSERT(false);
		}
		
		return s_instance;
	}
	
	public bhMouse getMouse()
	{
		return m_mouse;
	}
	
	public bhGridCoordinate getMouseGridCoord()
	{
		return m_mouseGridCoord;
	}
	
	public boolean isMouseTouchingSnappableCell()
	{
		return m_isMouseTouchingSnappableCell && m_mouse.isMouseOver();
	}
	
	public void onMouseEvent(bhMouseEvent event)
	{
		switch( event.getType())
		{
			case MOUSE_MOVE:
			{
				//updateMouse();
				
				break;
			}
			
			case MOUSE_DOWN:
			{
				if ( m_cameraState instanceof State_ViewingCell )
				{
					bhBufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
					
					mousePointToWorld(m_utilPoint1);
					
					if ( cell.isTouchingPoint(m_utilPoint1) )
					{
						m_mouseWentDownOnViewedCell = true;
						
						return;
					}
				}
				
				m_flickSmoother.clear();
				mousePointToWorld(m_grabPoint);
				m_lastWorldPoint.copy(bhCamera.getInstance().getPosition());
				
				m_mouseWentDownOnViewedCell = false;
				
				break;
			}
			
			/*case MOUSE_OVER:
			{
				if( m_mouseWasDownWhenWentOut )
				{
					m_isMouseDown = true;
					m_mouseWasDownWhenWentOut = false;
				}
				
				break;
			}*/
			
			case MOUSE_OUT:
			{
				break;
				/*if( !m_mouse.wasMouseJustDown() )
				{
					break;
				}*/
			}
			case MOUSE_UP:
			{
				//m_scrollArrow.hide();
				
				if ( !m_mouse.hasMouseStrayedWhileDown() )
				{
					mouseClicked();
				}
				else if ( m_cameraState instanceof State_CameraFloating )
				{
					m_flickSmoother.calcVelocity(m_utilVector);
				
					
					if( !m_utilVector.isZeroLength(null) )
					{
						m_utilVector.scaleByNumber(3);
						m_utilVector.setZ(0.0);
						m_utilPoint1.copy(bhCameraManager.getInstance().getTargetPosition());
						m_utilPoint1.translate(m_utilVector);
						
						m_setTargetArgs.initialize(m_utilPoint1, false);
						m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class, m_setTargetArgs);
					}
				}
				
				break;
			}
			
			case MOUSE_SCROLLED:
			{
				//--- Don't allow zoom out if mouse is over a viewed cell.
				if ( m_cameraState instanceof State_ViewingCell )
				{
					bhBufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
					
					mousePointToWorld(m_utilPoint2);
					
					if ( cell.isTouchingPoint(m_utilPoint2) )
					{
						return;
					}
				}
				
				bhCamera camera = bhCamera.getInstance();
				bhPoint cameraPosition = m_utilPoint1;
				cameraPosition.copy(camera.getPosition());
				
				if ( cameraPosition.getZ() == 0 && event.getScrollDelta() > 0 )
				{
					m_isZooming = false;
					
					return;
				}
				
				if ( event.getScrollDelta() < 0 )
				{
					m_isZooming = false;
				}
				
				bhA_State oldCameraState = m_cameraState;
				
				if ( m_cameraState instanceof State_CameraSnapping )
				{
					m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class);
				}
				else if ( m_cameraState instanceof State_ViewingCell )
				{
					if ( event.getScrollDelta() < 0 )
					{
						m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class);
					}
				}
				
				if ( m_cameraState instanceof State_CameraFloating )
				{
					if ( !m_isZooming )
					{
						if ( cameraPosition.getZ() > 0 && event.getScrollDelta() > 0 )
						{
							m_isZooming = true;
							
							m_mouseZoomPoint2d.copy(m_mouse.getMousePoint());
							mousePointToWorld(m_mouseZoomPoint3d);
						}
					}
					
					double scrollDelta = bhU_Math.clamp(event.getScrollDelta(), -SCROLL_CAP, SCROLL_CAP);
					scrollDelta *= BASE_SCROLL_SCALE;
					
					// TODO: Don't really like this zoom behavior...have to scale zom bsaed on distance somehow though.
					scrollDelta += bhU_Math.sign(scrollDelta) * camera.getPosition().getZ() * .5;
					
				//	trace(event.getScrollDelta(), scrollDelta, camera.getPosition().getZ());
					
					if ( m_isZooming )
					{
						m_mouseZoomPoint3d.calcDifference(cameraPosition, m_utilVector);
						double lengthSquared = m_utilVector.calcLengthSquared();
						m_utilVector.normalize();
						m_utilVector.scaleByNumber(scrollDelta);
						
						if ( m_utilVector.calcLengthSquared() > lengthSquared )
						{
							m_utilVector.setLength(Math.sqrt(lengthSquared));
						}
						
						//trace(m_utilVector);
						
						cameraPosition.add(m_utilVector);
						
						/*if( !(oldCameraState instanceof State_CameraSnapping) )
						{
							cameraPosition.setX(StateMachine_CameraController.IGNORED_COMPONENT);
							cameraPosition.setY(StateMachine_CameraController.IGNORED_COMPONENT);
						}*/
						
						m_setTargetArgs.initialize(cameraPosition, false);
						m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class, m_setTargetArgs);
					}
					else
					{
						/*bhPoint cameraPointProjected = m_utilPoint1;
						cameraPointProjected.copy(camera.getPosition());
						cameraPointProjected.setZ(0);
						mousePointInWorld.calcDifference(cameraPointProjected, m_utilVector);
						//trace(m_utilVector);*/
						
						cameraPosition.incZ( -scrollDelta);
						//cameraPosition.add(m_utilVector);
						
						/*if( !(oldCameraState instanceof State_CameraSnapping) )
						{
							cameraPosition.setX(StateMachine_CameraController.IGNORED_COMPONENT);
							cameraPosition.setY(StateMachine_CameraController.IGNORED_COMPONENT);
						}*/
						
						m_setTargetArgs.initialize(cameraPosition, false);
						m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class, m_setTargetArgs);
					}
				}
				
				break;
			}
		}
	}
	
	private void mousePointToWorld(bhPoint outPoint)
	{
		this.screenToWorld(m_mouse.getMousePoint(), outPoint);
	}
	
	private void screenToWorld(bhPoint screenPoint, bhPoint outPoint)
	{
		bhCamera camera = bhCamera.getInstance();
		
		outPoint.set(0, 0, 0);
		camera.calcWorldPoint(screenPoint, outPoint);
	}
	
	private void updateMouseGridCoord()
	{
		bhClientGrid grid = bhClientGrid.getInstance();
		double gridWidth = grid.calcPixelWidth();
		double gridHeight = grid.calcPixelHeight();
		
		
		mousePointToWorld(m_utilPoint2);
		
		if ( m_utilPoint2.getX() < 0 || m_utilPoint2.getX() > gridWidth )
		{
			m_mouseGridCoord.set(-1, -1);

			m_isMouseTouchingSnappableCell = false;
			
			return;
		}
		
		if ( m_utilPoint2.getY() < 0 || m_utilPoint2.getY() > gridHeight )
		{
			m_mouseGridCoord.set(-1, -1);

			m_isMouseTouchingSnappableCell = false;
			
			return;
		}
		
		//double cellSizePlusSpacing = bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
		
		//double modX = m_utilPoint2.getX() % cellSizePlusSpacing;
		//double modY = m_utilPoint2.getY() % cellSizePlusSpacing;
		
		m_mouseGridCoord.setWithPoint(m_utilPoint2, bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT);
		
		/*if ( modX > bhS_App.CELL_PIXEL_COUNT || modY > bhS_App.CELL_PIXEL_COUNT )
		{
			m_isMouseTouchingCell = false;
		}*/
		
		bhGridCoordinate viewedOrTargetCoord = null;
		
		if( m_cameraState instanceof State_ViewingCell )
		{
			bhBufferCell viewedCell = ((State_ViewingCell)m_cameraState).getCell();
			
			viewedOrTargetCoord = viewedCell.getCoordinate();
		}
		else if( m_cameraState instanceof State_CameraSnapping )
		{
			viewedOrTargetCoord = ((State_CameraSnapping)m_cameraState).getTargetCoordinate();
		}
		
		if( viewedOrTargetCoord != null && viewedOrTargetCoord.isEqualTo(m_mouseGridCoord) )
		{
			m_isMouseTouchingSnappableCell = false;
			return;
		}
		
		m_isMouseTouchingSnappableCell = true;
	}
	
	private void mouseClicked()
	{
		updateMouseGridCoord();
		
		if( !m_isMouseTouchingSnappableCell )  return;
		
		StateMachine_Camera.SnapToCoordinate.Args args = new StateMachine_Camera.SnapToCoordinate.Args(m_mouseGridCoord);
		m_cameraController.performAction(StateMachine_Camera.SnapToCoordinate.class, args);
	}
	
	private void updateMouse()
	{
		bhCamera camera = bhCamera.getInstance();

		updateMouseGridCoord(); // DRK > NOTE: This must be updated every frame, even with mouse still, mouse can still be moving in the world.
		
		if( m_cameraController.getUpdateCount() % 15 == 0 )
		{
			//s_logger.info("" + bhS_App.CELL_SPACING_PIXEL_COUNT * camera.calcDistanceRatio());
		}
		
		if ( m_mouse.isMouseDown() )
		{
			if ( m_cameraState instanceof State_CameraFloating )
			{
				bhPoint mouseDown3d = m_grabPoint;
				bhPoint mouseCurrent3d = m_utilPoint1;
				bhPoint camera3d = camera.getPosition();
				
				//camera.calcWorldPoint(m_mouse.getMouseDownPoint(), mouseDown3d);
				camera.calcWorldPoint(m_mouse.getMousePoint(), mouseCurrent3d);
				
				mouseCurrent3d.calcDifference(mouseDown3d, m_utilVector);
				m_utilVector.negate();
				
				m_utilPoint2.copy(camera3d);
				m_utilPoint2.translate(m_utilVector);
				
				//m_utilPoint2.setZ(StateMachine_CameraController.IGNORED_COMPONENT);
				
				//s_logger.info(m_utilVector.toString());
		
				
				//TODO: m_mouse.isMoving is busted.
				if( m_utilVector.calcLengthSquared() > 0)
				{
					m_setTargetArgs.initialize(m_utilPoint2, true);
					bhA_Action.perform(StateMachine_Camera.SetCameraTarget.class, m_setTargetArgs);
				}
				
				m_utilPoint2.calcDifference(m_lastWorldPoint, m_utilVector);
				m_flickSmoother.addVelocity(m_utilVector);
				m_lastWorldPoint.copy(m_utilPoint2);
				
				//if ( m_mouse.hasMouseStrayedWhileDown() && !m_mouse.isMouseMoving() )
				{
					//m_scrollArrow.show();
				}
				
				/*if ( m_scrollArrow.isShowing() )
				{
					m_scrollArrow.graphics.clear();
					m_scrollArrow.graphics.lineStyle(3, 0, .5);
					m_scrollArrow.graphics.moveTo(m_mouseCursor.getX(), m_mouseCursor.getY() );
					m_scrollArrow.graphics.lineTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
					
					m_mouseCursor.calcDifference(m_mouseDownPoint, m_utilVector);
					m_utilVector.setLength(10);
					m_utilVector.rotate(Math.PI / 4);
					m_scrollArrow.graphics.moveTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
					bhPoint drawPoint = m_mouseDownPoint.clone();
					drawPoint.add(m_utilVector);
					m_scrollArrow.graphics.lineTo(drawPoint.getX(), drawPoint.getY());
					m_utilVector.rotate( -Math.PI / 2)
					drawPoint.copy(m_mouseDownPoint);
					drawPoint.add(m_utilVector);
					m_scrollArrow.graphics.moveTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
					m_scrollArrow.graphics.lineTo(drawPoint.getX(), drawPoint.getY());
				}*/
			}
			
			if( m_mouse.hasMouseStrayedWhileDown() )
			{
				if ( m_cameraState instanceof State_CameraSnapping )
				{
					m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class);
				}
				else if ( m_cameraState instanceof State_ViewingCell )
				{
					if( !m_mouseWentDownOnViewedCell )
					{
						m_cameraController.performAction(StateMachine_Camera.SetCameraTarget.class);
					}
				}
			}
		}
		
		if ( this.m_isZooming )
		{
			if ( !m_mouse.getMousePoint().isEqualTo(m_mouseZoomPoint2d, MOUSE_TOLERANCE) )
			{
				this.m_isZooming = false;
			}
		}
	}
	
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if ( event.getState() instanceof StateMachine_Camera )
				{
					m_cameraController = (StateMachine_Camera) event.getState();
				}
				else if ( event.getState().getParent() instanceof StateMachine_Camera )
				{
					if( !event.getState().isTransparent() )
					{
						m_cameraState = event.getState();
					}
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if ( event.getState() instanceof StateMachine_Camera )
				{
					//m_cellManager.sync(bhCellBufferManager.getInstance()); // make sure cells are in correct initial position before rendering
				}
				break;
			}
			
			case DID_EXIT:
			{
				if ( event.getState() instanceof StateMachine_Camera )
				{
					m_cameraController = null;
				}
				else if ( event.getState() instanceof State_CameraFloating )
				{
					m_isZooming = false; // just a catch all....can only zoom while floating...zooming could even be its own state maybe
					//m_scrollArrow.hide();
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if ( event.getState().getParent() instanceof StateMachine_Camera )
				{
					//m_scrollArrow.update(state.getLastTimeStep());
					this.update();
				}
				
				break;
			}
		}
	}

	private void update()
	{
		m_mouse.update();
		updateMouse();
	}
}