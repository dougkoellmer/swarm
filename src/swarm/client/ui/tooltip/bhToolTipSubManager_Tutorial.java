package swarm.client.ui.tooltip;

import java.util.HashMap;

import swarm.client.ui.bhE_ZIndex;
import swarm.shared.debugging.bhU_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

class bhToolTipSubManager_Tutorial implements bhI_ToolTipSubManager
{
	private final bhToolTipManager m_toolTipManager;
	
	private final bhToolTip m_toolTip = new bhToolTip();
	
	private Element m_currentElement = null;
	
	private final HashMap<Element, bhToolTipConfig> m_tipMap = new HashMap<Element, bhToolTipConfig>();
	
	bhToolTipSubManager_Tutorial(bhToolTipManager toolTipManager)
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
	public void addTip(IsWidget widget, bhToolTipConfig config)
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
