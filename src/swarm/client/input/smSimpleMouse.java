package swarm.client.input;

import swarm.shared.structs.smPoint;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Widget;

public class smSimpleMouse implements MouseMoveHandler
{
	private final smPoint m_mousePoint = new smPoint();
	
	public smSimpleMouse(Widget widget)
	{
		widget.addDomHandler(this, MouseMoveEvent.getType());
	}

	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		m_mousePoint.set(event.getClientX(), event.getClientY(), 0);
	}
	
	public smPoint getMousePoint()
	{
		return m_mousePoint;
	}
}
