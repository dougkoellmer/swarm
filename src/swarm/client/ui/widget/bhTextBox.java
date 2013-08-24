package swarm.client.ui.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

public class bhTextBox extends TextBox implements bhI_TextBox
{
	private final bhTextBoxChangeDispatcher m_dispatcher = new bhTextBoxChangeDispatcher(this);
	
	public bhTextBox(String placeholder_nullable)
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
	
	public bhTextBox(Element element, String placeholder_nullable)
	{
		super(element);
		
		setPlaceholder(placeholder_nullable);
	}
	
	public bhTextBox(bhI_TextBoxChangeListener listener)
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
	public void setChangeListener(bhI_TextBoxChangeListener listener)
	{
		m_dispatcher.setListener(listener);
		
		if( listener != null )
		{
			sinkEvents(Event.ONPASTE);
		}
	}
}
