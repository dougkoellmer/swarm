package com.b33hive.client.input;

import com.b33hive.shared.structs.bhPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.user.client.ui.Widget;

public class bhClickManager
{
	private static bhClickManager s_instance;
	
	private final boolean m_isTouchOnlyInterface;
	
	private final bhPoint m_touchPoint = new bhPoint();
	
	private bhClickManager(boolean isTouchOnlyInterface)
	{
		m_isTouchOnlyInterface = isTouchOnlyInterface;
	}
	
	public static void startUp()
	{
		s_instance = new bhClickManager(false);
	}
	
	public static bhClickManager getInstance()
	{
		return s_instance;
	}
	
	public void addClickHandler(Widget widget, final bhI_ClickHandler handler)
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
