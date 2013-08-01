package com.b33hive.client.input;

import com.b33hive.shared.structs.bhPoint;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Widget;

public class bhSimpleMouse implements MouseMoveHandler
{
	private final bhPoint m_mousePoint = new bhPoint();
	
	public bhSimpleMouse(Widget widget)
	{
		widget.addDomHandler(this, MouseMoveEvent.getType());
	}

	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		m_mousePoint.set(event.getClientX(), event.getClientY(), 0);
	}
	
	public bhPoint getMousePoint()
	{
		return m_mousePoint;
	}
}
