package com.b33hive.client.ui.widget;

import java.util.logging.Logger;

import com.b33hive.client.app.bhS_ClientApp;
import com.b33hive.client.entities.bhCamera;
import com.b33hive.client.input.bhClickManager;
import com.b33hive.client.input.bhI_ClickHandler;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.states.camera.StateMachine_Camera;
import com.b33hive.client.states.camera.State_CameraFloating;
import com.b33hive.client.states.camera.State_CameraSnapping;
import com.b33hive.client.states.camera.State_ViewingCell;
import com.b33hive.client.ui.bhE_ZIndex;
import com.b33hive.client.ui.tooltip.bhE_ToolTipType;
import com.b33hive.client.ui.tooltip.bhToolTipConfig;
import com.b33hive.client.ui.tooltip.bhToolTipManager;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.bhU_Math;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhI_StateEventListener;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhPoint;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HumanInputEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class bhMagnifier extends FlowPanel implements bhI_StateEventListener
{
	private class MagnifierButton extends bhSpriteButton
	{
		public MagnifierButton(String spriteId)
		{
			super(spriteId);
			
			this.addStyleName("bh_zoom_button");
		}
	}
	
	private static final Logger s_logger = Logger.getLogger(bhMagnifier.class.getName());
	
	//--- DRK > These kind of "act" like constants which is why they're formatted as such, though they aren't technically.
	//---		They need to be derived at runtime.
	private double SLIDER_HEIGHT = Double.NaN;
	private double DRAGGER_START_Y = Double.NaN;
	private double DRAGGER_HEIGHT = Double.NaN;
	private double DRAGGER_HEIGHT_DIV_2 = Double.NaN;
	
	private final MagnifierButton m_zoomIn = new MagnifierButton("zoom_in");
	private final MagnifierButton m_zoomOut = new MagnifierButton("zoom_out");
	private final MagnifierButton m_dragger = new MagnifierButton("zoom_drag");
	private final bhButton m_slider = new bhButton();
	
	private bhA_State m_cameraState = null;
	
	private boolean m_underThisControl = false;
	
	private double m_currentRatio = 0;
	
	private boolean m_isMouseDown = false;
	private double m_mouseDownOffset = 0;
	private double m_touchY = 0;
	
	private final bhPoint m_utilPoint = new bhPoint();
	
	private final StateMachine_Camera.SetCameraTarget.Args m_args_SetCameraTarget = new StateMachine_Camera.SetCameraTarget.Args();

	public bhMagnifier()
	{
		this.addStyleName("bh_magnifier");
		m_zoomIn.addStyleName("bh_zoom_in_button");
		m_zoomOut.addStyleName("bh_zoom_out_button");
		m_dragger.addStyleName("bh_zoom_drag_button");
		m_slider.addStyleName("bh_zoom_slider");
		
		bhE_ZIndex.MAGNIFIER.assignTo(this);
		
		final FlowPanel middleContainer = new FlowPanel();
		final VerticalPanel innerContainer = new VerticalPanel();
		
		middleContainer.add(m_dragger);
		middleContainer.add(m_slider);
		
		innerContainer.add(m_zoomIn);
		innerContainer.add(middleContainer);
		innerContainer.add(m_zoomOut);
		
		bhE_ZIndex.MAGNIFIER_DRAG_BUTTON.assignTo(m_dragger);
		
		m_dragger.setVisible(false);
		m_zoomOut.setEnabled(false);
		m_zoomIn.setEnabled(false);
		m_slider.setEnabled(false);
		
		bhToolTipManager toolTipper = bhToolTipManager.getInstance();
		toolTipper.addTip(m_zoomIn, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Zoom In"));
		toolTipper.addTip(m_zoomOut, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Zoom Out"));
		toolTipper.addTip(m_dragger, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Drag'n'Zoom"));
		
		m_args_SetCameraTarget.setUserData(this.getClass());
		
		this.add(innerContainer);
		
		bhClickManager.getInstance().addClickHandler(m_zoomIn, new bhI_ClickHandler()
		{
			public void onClick()
			{
				if( !m_zoomIn.isEnabled() )  return;
				
				double newRatio = 0;
				double mod = m_currentRatio % bhS_ClientApp.MAGNIFIER_TICK_RATIO;
				if( mod > (bhS_ClientApp.MAGNIFIER_TICK_RATIO/2) )
				{
					newRatio = m_currentRatio - mod;
				}
				else
				{
					newRatio = (m_currentRatio - mod) - bhS_ClientApp.MAGNIFIER_TICK_RATIO;
				}
				
				newRatio = bhU_Math.clamp(newRatio, 0, 1);
				
				bhMagnifier.this.setDraggerPosition(newRatio, true);
			}
		});
		
		bhClickManager.getInstance().addClickHandler(m_zoomOut, new bhI_ClickHandler()
		{
			public void onClick()
			{
				if( !m_zoomOut.isEnabled() )  return;
				
				double newRatio = 0;
				double mod = m_currentRatio % bhS_ClientApp.MAGNIFIER_TICK_RATIO;
				if( mod < (bhS_ClientApp.MAGNIFIER_TICK_RATIO/2) )
				{
					newRatio = (m_currentRatio - mod) + bhS_ClientApp.MAGNIFIER_TICK_RATIO;
				}
				else
				{
					newRatio = (m_currentRatio - mod) + bhS_ClientApp.MAGNIFIER_TICK_RATIO*2;
				}
				
				newRatio = bhU_Math.clamp(newRatio, 0, 1);
				
				bhMagnifier.this.setDraggerPosition(newRatio, true);
			}
		});
		
		m_slider.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				double mouseY = event.getRelativeY(bhMagnifier.this.m_slider.getElement());
				bhMagnifier.this.setDraggerPositionFromMouse(mouseY);
			}
		});
		
		m_dragger.addMouseDownHandler(new MouseDownHandler()
		{
			@Override
			public void onMouseDown(MouseDownEvent event)
			{
				onDraggerMouseDown(event, event.getRelativeY(bhMagnifier.this.m_slider.getElement()));
			}
		});
		
		m_dragger.addDomHandler(new TouchStartHandler()
		{
			@Override
			public void onTouchStart(TouchStartEvent event)
			{
				if( event.getTouches().length() > 1 )  return;
				
				Touch touch = event.getTouches().get(0);
				m_touchY = touch.getRelativeY(bhMagnifier.this.m_slider.getElement());
				onDraggerMouseDown(event, m_touchY);
			}
		}, TouchStartEvent.getType());
		
		
		m_dragger.addMouseMoveHandler(new MouseMoveHandler()
		{
			@Override
			public void onMouseMove(MouseMoveEvent event)
			{
				onDraggerMouseMove(event, event.getRelativeY(bhMagnifier.this.m_slider.getElement()));
			}
		});
		m_dragger.addDomHandler(new TouchMoveHandler()
		{
			@Override
			public void onTouchMove(TouchMoveEvent event)
			{
				Touch touch = event.getTouches().get(0);
				m_touchY = touch.getRelativeY(bhMagnifier.this.m_slider.getElement());
				onDraggerMouseMove(event, m_touchY);
			}
		}, TouchMoveEvent.getType());
		
		
		m_dragger.addMouseUpHandler(new MouseUpHandler()
		{
			@Override
			public void onMouseUp(MouseUpEvent event)
			{
				onDraggerMouseUp(event, event.getRelativeY(bhMagnifier.this.m_slider.getElement()));
			}
		});
		
		m_dragger.addDomHandler(new TouchEndHandler()
		{
			@Override
			public void onTouchEnd(TouchEndEvent event)
			{
				if(event.getTouches().length() == 0 )
				{
					onDraggerMouseUp(event, m_touchY);
				}
			}
		}, TouchEndEvent.getType());
	}
	
	private void onDraggerMouseDown(HumanInputEvent event, double relativeY)
	{
		Event.setCapture(bhMagnifier.this.m_dragger.getElement());
		event.preventDefault();
		
		m_isMouseDown = true;
		
		 //--- DRK > No idea why draggerTop has to be offset by half the dragger height...NO IDEA.
		double draggerTop = m_dragger.getAbsoluteTop() - DRAGGER_HEIGHT_DIV_2;
		double draggerY = (draggerTop - DRAGGER_START_Y) + DRAGGER_HEIGHT_DIV_2;
		double mouseY = relativeY;
		
		setDraggerPositionFromMouse(draggerY);
		
		m_mouseDownOffset = draggerY - mouseY;
	}
	
	private void onDraggerMouseMove(HumanInputEvent event, double relativeY)
	{
		if( m_isMouseDown )
		{
			double mouseY = relativeY;
			bhMagnifier.this.setDraggerPositionFromMouse(mouseY + m_mouseDownOffset);
			
			event.preventDefault();
		}
	}
	
	private void onDraggerMouseUp(HumanInputEvent event, double relativeY)
	{
		if( m_isMouseDown )
		{
			Event.releaseCapture(bhMagnifier.this.m_dragger.getElement());
			event.preventDefault();
			
			//--- DRK > Pretty hacky, but I can't figure out how to "forward" the event up the DOM so tooltip can get it.
			if( event instanceof MouseUpEvent )
			{
				bhToolTipManager.getInstance().onMouseUp((MouseUpEvent)event);
			}
			
			double mouseY = relativeY;
			bhMagnifier.this.setDraggerPositionFromMouse(mouseY + m_mouseDownOffset);
			
			m_isMouseDown = false;
		}
	}
	
	private void initValues()
	{
		SLIDER_HEIGHT = m_slider.getOffsetHeight();
		DRAGGER_START_Y = m_zoomIn.getElement().getOffsetHeight()-1;
		DRAGGER_HEIGHT = 18;//m_dragger.getOffsetHeight(); TODO: UGH, can't get correct value from DOM here for some reason.
		DRAGGER_HEIGHT_DIV_2 = DRAGGER_HEIGHT/2;
	}
	
	private void setDraggerPositionFromMouse(double mouseY)
	{
		mouseY -= DRAGGER_HEIGHT_DIV_2;
		double clickArea = (SLIDER_HEIGHT - DRAGGER_HEIGHT);
		double ratio = mouseY / clickArea;
		ratio = bhU_Math.clamp(ratio, 0, 1);
		
		this.setDraggerPosition(ratio, true);
	}
	
	private void setDraggerPositionFromCamera()
	{
		bhCamera camera = bhCamera.getInstance();
		double maxZ = camera.calcMaxZ();
		double ratio = camera.getPosition().getZ() / maxZ;
		ratio = bhU_Math.clamp(ratio, 0, 1); // window resizes can make camera be temporarily zoomed out further than its max constraint.
		ratio = Math.sqrt(ratio);
		
		/*s_logger.info("ratio: " + ratio);
		s_logger.info("z: " + camera.getPosition().getZ());
		s_logger.info("maxZ: " + maxZ);
		s_logger.info("-----");*/
		
		this.setDraggerPosition(ratio, false);
	}
	
	private void setDraggerPosition(double ratio, boolean moveCamera)
	{
		if( m_currentRatio == 0 )
		{
			if( ratio > 0 )
			{
				m_zoomIn.setEnabled(true);
			}
		}
		else if( m_currentRatio == 1 )
		{
			if( ratio < 1 )
			{
				m_zoomOut.setEnabled(true);
			}
		}
		
		if( ratio == 0 )
		{
			m_zoomIn.setEnabled(false);
		}
		else if( ratio == 1 )
		{
			m_zoomOut.setEnabled(false);
		}
		
		double sliderPosition = (SLIDER_HEIGHT-DRAGGER_HEIGHT)*ratio;
		sliderPosition = DRAGGER_START_Y + sliderPosition;
		sliderPosition = Math.round(sliderPosition);
		m_dragger.getElement().getStyle().setTop(sliderPosition, Unit.PX);
		
		if( moveCamera )
		{
			bhCamera camera = bhCamera.getInstance();
			double maxZ = camera.calcMaxZ();
			m_utilPoint.copy(camera.getPosition());
			m_utilPoint.setZ((ratio*ratio)*maxZ);
			
			boolean setTarget = false;
			
			if ( m_cameraState instanceof State_ViewingCell )
			{
				if ( ratio > 0 )
				{
					setTarget = true;
				}
			}
			else
			{
				setTarget = true;
			}
			
			if ( setTarget )
			{
				m_args_SetCameraTarget.setPoint(m_utilPoint);
				bhA_Action.perform(StateMachine_Camera.SetCameraTarget.class, m_args_SetCameraTarget);
			}
		}
		
		m_currentRatio = ratio;
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof StateMachine_Camera )
				{
					initValues();
					setDraggerPositionFromCamera();
					
					m_dragger.setVisible(true); // only made visible here so it doesn't appear out of place initially.
					
					m_zoomOut.setEnabled(true);
					m_zoomIn.setEnabled(true);
					m_slider.setEnabled(true);
				}
				else if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					m_cameraState = event.getState();
					
					if( m_cameraState instanceof State_CameraSnapping )
					{
						m_underThisControl = false;
					}
				}
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState() instanceof StateMachine_Camera )
				{
					if( !m_underThisControl )
					{
						if( !((StateMachine_Camera) event.getState()).getCameraManager().isCameraAtRest() )
						{
							setDraggerPositionFromCamera();
						}
					}
				}
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Camera.SetCameraTarget.class )
				{
					if( event.getActionArgs() != null )
					{
						if( event.getActionArgs().getUserData() != bhMagnifier.class )
						{
							m_underThisControl = false;
						}
						else
						{
							m_underThisControl = true;
						}
					}
					else
					{
						m_underThisControl = false;
					}
				}
				else if( event.getAction()  == StateMachine_Camera.SetCameraViewSize.class )
				{
					this.setDraggerPositionFromCamera();
				}
				else if( event.getAction()  == StateMachine_Base.OnGridResize.class )
				{
					this.setDraggerPositionFromCamera();
				}
				else if( event.getAction() == StateMachine_Camera.SetInitialPosition.class )
				{
					this.setDraggerPositionFromCamera();
				}
				
				break;
			}
		}
	}
}
