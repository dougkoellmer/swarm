package swarm.client.input;

import swarm.shared.structs.Point;
import com.google.gwt.dom.client.Element;

public class MouseEvent
{
	private E_MouseEventType m_eType = null;
	private int m_scrollDelta;
	private final Point m_point = new Point();
	private Element m_target;
	private boolean m_isTouch;
	
	public void set(E_MouseEventType eType, int scrollDelta, Element target, boolean isTouch)
	{
		m_eType = eType;
		m_scrollDelta = scrollDelta;
		m_target = target;
		m_isTouch = isTouch;
	}
	
	public Point getPoint()
	{
		return m_point;
	}
	
	public Element getTarget()
	{
		return m_target;
	}
	
	public E_MouseEventType getType()
	{
		return m_eType;
	}
	
	public int getScrollDelta()
	{
		return m_scrollDelta;
	}
	
	public boolean isTouch()
	{
		return m_isTouch;
	}
}
