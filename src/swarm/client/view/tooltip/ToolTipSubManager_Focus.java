package swarm.client.view.tooltip;

import java.util.HashMap;

import swarm.client.view.E_ZIndex;
import swarm.shared.debugging.U_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

class ToolTipSubManager_Focus implements FocusHandler, BlurHandler, I_ToolTipSubManager
{
	private final ToolTipManager m_toolTipManager;
	
	private final ToolTip m_toolTip = new ToolTip();
	
	private boolean m_isFocused = false;
	
	private Element m_currentElement = null;
	
	private final HashMap<Element, ToolTipConfig> m_tipMap = new HashMap<Element, ToolTipConfig>();
	
	ToolTipSubManager_Focus(ToolTipManager toolTipManager)
	{
		m_toolTipManager = toolTipManager;
	}
	
	@Override
	public void onBlur(BlurEvent event)
	{
		if( !m_isFocused )
		{
			//smU_Debug.ASSERT(false, "onBlur1");
			return;
		}
		
		m_toolTipManager.endTip(m_toolTip);
		
		m_currentElement = null;
		
		m_isFocused = false;
	}

	@Override
	public void onFocus(FocusEvent event)
	{
		if( m_isFocused )
		{
			//smU_Debug.ASSERT(false, "onFocus1");
			return;
		}

		m_currentElement = event.getRelativeElement();
		
		ToolTipConfig config = m_tipMap.get(m_currentElement);
		
		U_Debug.ASSERT(config!= null, "smToolTipSubManager_Focus::onFocus1");

		m_toolTipManager.prepareTip(m_toolTip, m_currentElement, config);
		
		m_toolTipManager.showTip(m_toolTip, null);
		
		m_isFocused = true;
	}

	@Override
	public void onExternalMouseUp(MouseUpEvent event)
	{
		// NOOP
	}

	@Override
	public void onGlobalMouseDown(MouseDownEvent event)
	{
		Element target = Element.as(event.getNativeEvent().getEventTarget());
		
		if( m_currentElement != null && target != m_currentElement )
		{
			m_currentElement.blur();
		}
	}

	@Override
	public void addTip(IsWidget widget, ToolTipConfig config)
	{
		m_tipMap.put(widget.asWidget().getElement(), config);
		
		widget.asWidget().addDomHandler(this, FocusEvent.getType());
		widget.asWidget().addDomHandler(this, BlurEvent.getType());
	}

	@Override
	public void update(double timeStep)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTip(IsWidget widget)
	{
		// TODO Auto-generated method stub
		
	}
}
