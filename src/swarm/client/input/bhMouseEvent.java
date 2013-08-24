package swarm.client.input;

import swarm.shared.structs.bhPoint;
import com.google.gwt.dom.client.Element;

public class bhMouseEvent
{
	private bhE_MouseEventType m_eType = null;
	private int m_scrollDelta;
	private final bhPoint m_point = new bhPoint();
	private Element m_target;
	private boolean m_isTouch;
	
	public void set(bhE_MouseEventType eType, int scrollDelta, Element target, boolean isTouch)
	{
		m_eType = eType;
		m_scrollDelta = scrollDelta;
		m_target = target;
		m_isTouch = isTouch;
	}
	
	public bhPoint getPoint()
	{
		return m_point;
	}
	
	public Element getTarget()
	{
		return m_target;
	}
	
	public bhE_MouseEventType getType()
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
