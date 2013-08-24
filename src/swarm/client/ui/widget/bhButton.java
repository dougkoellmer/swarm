package swarm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

public class bhButton extends FlowPanel
{
	private boolean m_enabled = true;
	
	public bhButton()
	{
		this.addStyleName("sm_button");
	}
	
	public void addClickHandler(ClickHandler handler)
	{
		addDomHandler(handler, ClickEvent.getType());
	}
	
	public void addDragStartHandler(DragStartHandler handler)
	{
	    addBitlessDomHandler(handler, DragStartEvent.getType());
	}
	
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
	    return addDomHandler(handler, MouseDownEvent.getType());
	  }
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
	    return addDomHandler(handler, MouseMoveEvent.getType());
	  }
	 public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		    return addDomHandler(handler, MouseUpEvent.getType());
		  }
	public void setEnabled(boolean enabled)
	{
		m_enabled = enabled;
		
		DOM.setElementPropertyBoolean(getElement(), "disabled", !enabled);
		if( enabled )
		{
			getElement().removeAttribute("disabled");
		}
		else
		{
			getElement().setAttribute("disabled", "disabled");
		}
	}
	
	public boolean isEnabled()
	{
		return m_enabled;
	}
}
