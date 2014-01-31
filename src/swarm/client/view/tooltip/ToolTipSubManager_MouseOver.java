package swarm.client.view.tooltip;

import java.util.HashMap;

import swarm.shared.debugging.U_Debug;
import swarm.shared.structs.Point;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

class ToolTipSubManager_MouseOver implements I_ToolTipSubManager, MouseOverHandler, MouseOutHandler, MouseMoveHandler, MouseDownHandler, MouseUpHandler, ClickHandler
{
	private final ToolTipManager m_toolTipManager;
	
	private final ToolTip m_toolTip = new ToolTip();
	
	private int m_mouseX;
	private int m_mouseY;
	
	HandlerRegistration m_moveHandler;
	HandlerRegistration m_upHandler;
	
	boolean m_isMouseDown = false;
	
	private Element m_currentTargetElement = null;
	
	private static final Point s_mousePoint = new Point();
	
	private final HashMap<Element, ToolTipConfig> m_tipMap = new HashMap<Element, ToolTipConfig>();
	
	private final int m_delayMilliseconds;
	
	private final Timer m_timer = new Timer()
	{
		@Override
		public void run()
		{
			s_mousePoint.set(m_mouseX, m_mouseY, 0);
			
			m_toolTipManager.showTip(m_toolTip, s_mousePoint);
			
			RootPanel.get().addDomHandler(ToolTipSubManager_MouseOver.this, MouseMoveEvent.getType());
		}
	};
	
	ToolTipSubManager_MouseOver(ToolTipManager toolTipManager, int delayMilliseconds)
	{
		m_delayMilliseconds = delayMilliseconds;
		m_toolTipManager = toolTipManager;
	}
	
	private void addMoveHandler()
	{
		if( m_moveHandler != null )
		{
			//--- DRK > NOTE: Should prolly be an smU_Debug.ASSERT case normally, but the whole event flow is spaghettied, so screw it.
			
			return;
		}
		
		m_moveHandler = RootPanel.get().addDomHandler(this, MouseMoveEvent.getType());
	}
	
	private void addUpHandler()
	{
		if( m_upHandler != null )
		{
			//smU_Debug.ASSERT(false); // TODO: something's hitting this...no idea why
			
			return;
		}
		
		m_upHandler = RootPanel.get().addDomHandler(this, MouseUpEvent.getType());

	}
	
	private void removeMoveHandler()
	{
		if( m_moveHandler == null )  return;
		
		m_moveHandler.removeHandler();
		
		m_moveHandler = null;
	}
	
	private void removeUpHandler()
	{
		if( m_upHandler == null )  return;
		
		m_upHandler.removeHandler();
		
		m_upHandler = null;
	}
	
	private void cancel()
	{
		m_timer.cancel();

		m_toolTipManager.endTip(m_toolTip);
		
		m_currentTargetElement = null;

		removeMoveHandler();
	}
	
	@Override
	public void onMouseOut(MouseOutEvent event)
	{
		cancel();
	}
	
	@Override
	public void onClick(ClickEvent event)
	{
		//--- DRK > Covers fringe case of being moused over a button,
		//---		then programmatically clicking the button, like through a keypress.
		cancel();
	}

	@Override
	public void onMouseOver(MouseOverEvent event)
	{
		if( m_isMouseDown )  return;
		
		Element element = event.getRelativeElement();
		
		ToolTipConfig config = m_tipMap.get(element);
		
		U_Debug.ASSERT(config!= null, "smToolTipSubManager_MouseOver::onMouseOver1");

		m_toolTipManager.prepareTip(m_toolTip, element, config);

		m_mouseX = event.getClientX();
		m_mouseY = event.getClientY();
		
		addMoveHandler();
		
		m_currentTargetElement = element;
		m_timer.schedule(m_delayMilliseconds);
	}
	
	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		m_mouseX = event.getClientX();
		m_mouseY = event.getClientY();
	}

	@Override
	public void onMouseDown(MouseDownEvent event)
	{
		m_isMouseDown = true;
		
		addUpHandler();
		
		cancel();
	}

	@Override
	public void onMouseUp(MouseUpEvent event)
	{
		internal_onMouseUp(event);
	}
	
	void internal_onMouseUp(MouseUpEvent event)
	{
		m_isMouseDown = false;
		
		removeUpHandler();
	}
	
	@Override
	public void onExternalMouseUp(MouseUpEvent event)
	{
		this.onMouseUp(event);
	}

	@Override
	public void onGlobalMouseDown(MouseDownEvent event)
	{
		// NOOP
	}

	@Override
	public void addTip(IsWidget widget, ToolTipConfig config)
	{
		Element element = widget.asWidget().getElement();
		if( !m_tipMap.containsKey(element) )
		{
			widget.asWidget().addDomHandler(this, MouseOverEvent.getType());
			widget.asWidget().addDomHandler(this, MouseOutEvent.getType());
			widget.asWidget().addDomHandler(this, MouseDownEvent.getType());
			widget.asWidget().addDomHandler(this, ClickEvent.getType());
		}
		
		m_tipMap.put(element, config); // replace the config no matter what.
	}

	@Override
	public void update(double timeStep)
	{
		//TODO: Have this drive the timing needs for this class
	}

	@Override
	public void removeTip(IsWidget widget)
	{
	}
}
