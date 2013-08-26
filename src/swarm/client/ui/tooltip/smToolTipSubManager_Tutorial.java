package swarm.client.ui.tooltip;

import java.util.HashMap;

import swarm.client.ui.smE_ZIndex;
import swarm.shared.debugging.smU_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

class smToolTipSubManager_Tutorial implements smI_ToolTipSubManager
{
	private final smToolTipManager m_toolTipManager;
	
	private final smToolTip m_toolTip = new smToolTip();
	
	private Element m_currentElement = null;
	
	private final HashMap<Element, smToolTipConfig> m_tipMap = new HashMap<Element, smToolTipConfig>();
	
	bhToolTipSubManager_Tutorial(smToolTipManager toolTipManager)
	{
		m_toolTipManager = toolTipManager;
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
	public void addTip(IsWidget widget, smToolTipConfig config)
	{
		m_tipMap.put(widget.asWidget().getElement(), config);
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
