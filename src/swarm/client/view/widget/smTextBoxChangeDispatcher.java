package swarm.client.view.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBox;

public class smTextBoxChangeDispatcher
{
	private smI_TextBoxChangeListener m_listener;
	
	public smTextBoxChangeDispatcher(final TextBox textBox)
	{
		textBox.addKeyDownHandler(new KeyDownHandler()
		{
			@Override
			public void onKeyDown(KeyDownEvent event)
			{
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER )
				{
					dispatchEnterPressed();
				}
				else
				{
					dispatchTextChangeDeferred();
				}
				
				event.preventDefault();
			}
		});
		
		/*textBox.addKeyUpHandler(new KeyUpHandler()
		{
			@Override
			public void onKeyUp(KeyUpEvent event)
			{
			}
		});*/
	}
	
	native void dispatchTextChangeDeferred()
	/*-{
		var _this = this;
		$wnd.setTimeout(function()
		{
			_this.@swarm.client.view.widget.smTextBoxChangeDispatcher::dispatchTextChange()();
		}, 0);
	}-*/;
	
	void dispatchTextChange()
	{
		if( m_listener != null )
		{
			m_listener.onTextChange();
		}
	}
	
	void dispatchEnterPressed()
	{
		if( m_listener != null )
		{
			m_listener.onEnterPressed();
		}
	}
	
	void setListener(smI_TextBoxChangeListener listener)
	{
		m_listener = listener;
	}
}
