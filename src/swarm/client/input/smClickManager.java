package swarm.client.input;

import swarm.shared.structs.smPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.user.client.ui.Widget;

public class smClickManager
{
	private final smPoint m_touchPoint = new smPoint();
	
	public smClickManager()
	{
	}
	
	public void addClickHandler(Widget widget, final smI_ClickHandler handler)
	{
		widget.addDomHandler(new MouseUpHandler()
		{
			@Override
			public void onMouseUp(MouseUpEvent event)
			{
				handler.onClick();
			}
			
		}, MouseUpEvent.getType());
		
		widget.addDomHandler(new TouchEndHandler()
		{
			@Override
			public void onTouchEnd(TouchEndEvent event)
			{
				event.preventDefault();
				
				handler.onClick();
			}
			
		}, TouchEndEvent.getType());
		
		/*if( m_isTouchOnlyInterface )
		{
			widget.addDomHandler(new TouchEndHandler()
			{
				@Override
				public void onTouchEnd(TouchEndEvent event)
				{
					handler.onClick();
				}
				
			}, TouchEndEvent.getType());
		}
		else
		{
			widget.addDomHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					handler.onClick();
				}
				
			}, ClickEvent.getType());
		}*/
	}
}
