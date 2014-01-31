package swarm.client.view.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

public class TextField extends TextBox implements I_TextBox
{
	private final TextBoxChangeDispatcher m_dispatcher = new TextBoxChangeDispatcher(this);
	
	public TextField(String placeholder_nullable)
	{
		setPlaceholder(placeholder_nullable);
	}
	
	private void setPlaceholder(String value_nullable)
	{
		if( value_nullable != null )
		{
			this.getElement().setAttribute("placeholder", value_nullable);
		}
	}
	
	public TextField(Element element, String placeholder_nullable)
	{
		super(element);
		
		setPlaceholder(placeholder_nullable);
	}
	
	public TextField(I_TextBoxChangeListener listener)
	{
		setChangeListener(listener);
	}
	
	@Override
	public void onBrowserEvent(Event event)
	{
		super.onBrowserEvent(event);
		
		switch (event.getTypeInt())
		{
			case Event.ONPASTE:
			{
				m_dispatcher.dispatchTextChangeDeferred();
				
				break;
			}
		}
	}

	@Override
	public void setChangeListener(I_TextBoxChangeListener listener)
	{
		m_dispatcher.setListener(listener);
		
		if( listener != null )
		{
			sinkEvents(Event.ONPASTE);
		}
	}
}
