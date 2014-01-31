package swarm.client.input;

import swarm.shared.structs.Point;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Widget;

public class SimpleMouse implements MouseMoveHandler
{
	private final Point m_mousePoint = new Point();
	
	public SimpleMouse(Widget widget)
	{
		widget.addDomHandler(this, MouseMoveEvent.getType());
	}

	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		m_mousePoint.set(event.getClientX(), event.getClientY(), 0);
	}
	
	public Point getMousePoint()
	{
		return m_mousePoint;
	}
}
