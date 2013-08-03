package b33hive.client.input;

import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.client.input.bhE_MouseEventType;
import b33hive.client.input.bhMouseEvent;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.structs.bhTolerance;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class bhMouse implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler, MouseOverHandler, MouseWheelHandler, TouchStartHandler, TouchEndHandler, TouchMoveHandler
{
	public static interface I_Listener
	{
		void onMouseEvent(bhMouseEvent event);
	}
	
	private static final Logger s_logger = Logger.getLogger(bhMouse.class.getName());
	
	private final bhMouseEvent m_mouseEvent = new bhMouseEvent();
	
	private Widget m_container = null;
	
	private boolean m_isMouseDown = false;
	private boolean m_hasMouseStrayedWhileDown = false;
	private boolean m_wasMouseJustDown = false;
	private boolean m_isMouseMoving = false;
	private boolean m_isMouseOver = false;
	
	private boolean m_isTouching = false;
	
	private final bhPoint m_mousePoint = new bhPoint();
	private final bhPoint m_mouseDownPoint = new bhPoint();
	private final bhPoint m_lastMousePoint = new bhPoint();
	
	private I_Listener m_listener = null;
	
	public bhMouse(Widget container)
	{
		s_logger.setLevel(Level.OFF);
		
		m_container = container;
		
		addMouseDownHandlers();
	}
	
	public void setListener(I_Listener listener)
	{
		m_listener = listener;
	}
	
	public boolean isMouseOver()
	{
		return m_isMouseOver;
	}
	
	public boolean isMouseMoving()
	{
		return m_isMouseMoving;
	}
	
	public boolean wasMouseJustDown()
	{
		return m_wasMouseJustDown;
	}
	
	public boolean isMouseDown()
	{
		return m_isMouseDown;
	}
	
	public boolean hasMouseStrayedWhileDown()
	{
		return m_hasMouseStrayedWhileDown;
	}
	
	public bhPoint getMousePoint()
	{
		return m_mousePoint;
	}
	
	public bhPoint getMouseDownPoint()
	{
		if( m_isMouseDown )
		{
			return m_mouseDownPoint;
		}
		else
		{
			bhU_Debug.ASSERT(false);
			return null;
		}
	}
	
	private void addMouseDownHandlers()
	{
		if( m_container.getParent() != null )
		{
			m_container.getParent().addDomHandler(this, MouseWheelEvent.getType()); // TODO: Have to add to parent so zoom works over hud...HACKY
		}
		
		m_container.addDomHandler(this, MouseOverEvent.getType());
		m_container.addDomHandler(this, MouseOutEvent.getType());
		m_container.addDomHandler(this, MouseMoveEvent.getType());
		m_container.addDomHandler(this, MouseUpEvent.getType());
		m_container.addDomHandler(this, MouseDownEvent.getType());
		m_container.addDomHandler(this, TouchStartEvent.getType());
		m_container.addDomHandler(this, TouchEndEvent.getType());
		m_container.addDomHandler(this, TouchMoveEvent.getType());
	}
	
	public void update()
	{
		if( !m_lastMousePoint.isEqualTo(m_mousePoint, bhTolerance.EXACT) )
		{
			m_isMouseMoving = true;
		}
		else
		{
			m_isMouseMoving = false;
		}
		
		m_lastMousePoint.copy(m_mousePoint);
	}
	
	public void dispatchEvent(bhMouseEvent event)
	{
		m_mousePoint.copy(event.getPoint());
		
		if( m_isMouseDown )
		{
			if( !m_mouseDownPoint.isEqualTo(m_mousePoint, bhTolerance.EXACT) )
			{
				m_hasMouseStrayedWhileDown = true;
			}
		}

		if( m_listener != null )
		{
			m_listener.onMouseEvent(event);
		}
		
		m_lastMousePoint.copy(m_mousePoint);
		
		m_wasMouseJustDown = false;
	}
	
	private void setMousePoint(double x, double y, bhPoint outPoint)
	{
		//--- DRK > This method used to do more...
		outPoint.set(x, y, 0);
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event)
	{
		this.setMousePoint(event.getRelativeX(m_container.getElement()), event.getRelativeY(m_container.getElement()), m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_SCROLLED, -event.getDeltaY(), Element.as(event.getNativeEvent().getEventTarget()), false);
		
		s_logger.log(Level.INFO, "scroll" + m_mouseEvent.getScrollDelta());
		
		dispatchEvent(m_mouseEvent);
	}

	@Override
	public void onMouseOver(MouseOverEvent event)
	{
		if( m_isTouching )  return;
		
		s_logger.info("over");

		m_isMouseOver = true;
		
		this.setMousePoint(event.getRelativeX(m_container.getElement()), event.getRelativeY(m_container.getElement()), m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_OVER, 0, Element.as(event.getNativeEvent().getEventTarget()), false);
		
		dispatchEvent(m_mouseEvent);
	}

	@Override
	public void onMouseOut(MouseOutEvent event)
	{
		if( m_isTouching )  return;
		
		s_logger.log(Level.INFO, "out");
	
		m_isMouseOver = false;

		this.setMousePoint(event.getRelativeX(m_container.getElement()), event.getRelativeY(m_container.getElement()), m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_OUT, 0, Element.as(event.getNativeEvent().getEventTarget()), false);
		
		dispatchEvent(m_mouseEvent);
	}

	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		if( m_isTouching )  return;
		
		s_logger.log(Level.SEVERE, "move");
		
		this.setMousePoint(event.getRelativeX(m_container.getElement()), event.getRelativeY(m_container.getElement()), m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_MOVE, 0, Element.as(event.getNativeEvent().getEventTarget()), false);
		
		dispatchEvent(m_mouseEvent);
	}

	@Override
	public void onMouseUp(MouseUpEvent event)
	{
		onMouseOrTouchUp(event, event.getRelativeX(m_container.getElement()), event.getRelativeY(m_container.getElement()), false);
	}

	@Override
	public void onMouseDown(MouseDownEvent event)
	{
		onMouseOrTouchDown(event, event.getX(), event.getY(), false);
	}
	
	private void onMouseOrTouchUp(DomEvent event, int x, int y, boolean isTouch)
	{
		s_logger.log(Level.INFO, "up");

		if( !m_isMouseDown )
		{
			return;
		}
		
		m_wasMouseJustDown = m_isMouseDown; // should always be the case;
		m_isMouseDown = false;
	
		this.setMousePoint(x, y, m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_UP, 0, Element.as(event.getNativeEvent().getEventTarget()), isTouch);
		
		dispatchEvent(m_mouseEvent);
		
		Event.releaseCapture(m_container.getElement());
		event.preventDefault();
	}
	
	@Override
	public void onTouchStart(TouchStartEvent event)
	{
		if( m_isMouseDown )  return;
		
		Touch touch = event.getTouches().get(0);
		
		onMouseOrTouchDown(event, touch.getRelativeX(m_container.getElement()), touch.getRelativeY(m_container.getElement()), true);
	}

	@Override
	public void onTouchEnd(TouchEndEvent event)
	{
		m_isTouching = event.getTouches().length() != 0;
		
		if( !m_isMouseDown )  return;
		
		onMouseOrTouchUp(event, (int)m_mousePoint.getX(), (int)m_mousePoint.getY(), true);
	}

	@Override
	public void onTouchMove(TouchMoveEvent event)
	{
		s_logger.log(Level.INFO, "move");
	
		Touch touch = event.getTouches().get(0);
		
		this.setMousePoint(touch.getRelativeX(m_container.getElement()), touch.getRelativeY(m_container.getElement()), m_mouseEvent.getPoint());
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_MOVE, 0, Element.as(event.getNativeEvent().getEventTarget()), true);
		
		dispatchEvent(m_mouseEvent);
	}
	
	private void onMouseOrTouchDown(DomEvent event, int x, int y, boolean isTouch)
	{
		m_isTouching = isTouch;
		
		s_logger.log(Level.INFO, "down");
		
		m_isMouseDown = true;
		m_hasMouseStrayedWhileDown = false;
		
		setMousePoint(x, y, m_mouseDownPoint);
		
		m_mouseEvent.getPoint().copy(m_mouseDownPoint);
		m_mouseEvent.set(bhE_MouseEventType.MOUSE_DOWN, 0, Element.as(event.getNativeEvent().getEventTarget()), isTouch);
		dispatchEvent(m_mouseEvent);

		Event.setCapture(m_container.getElement());
		event.preventDefault();
	}
}
