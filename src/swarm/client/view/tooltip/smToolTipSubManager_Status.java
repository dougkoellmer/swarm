package swarm.client.view.tooltip;

import java.util.HashMap;

import swarm.client.view.smE_ZIndex;
import swarm.shared.debugging.smU_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

class smToolTipSubManager_Status implements smI_ToolTipSubManager
{
	private final smToolTipManager m_toolTipManager;
	
	private final HashMap<Element, smToolTip> m_tipMap = new HashMap<Element, smToolTip>();
	
	smToolTipSubManager_Status(smToolTipManager toolTipManager)
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
		//Element target = Element.as(event.getNativeEvent().getEventTarget());
	}

	@Override
	public void addTip(IsWidget widget, smToolTipConfig config)
	{
		Element targetElement = widget.asWidget().getElement();
		smToolTip toolTip = new smToolTip();
		m_toolTipManager.prepareTip(toolTip, targetElement, config);
		m_toolTipManager.showTip(toolTip, null);
		m_tipMap.put(targetElement, toolTip);
	}

	@Override
	public void update(double timeStep)
	{

	}

	@Override
	public void removeTip(IsWidget widget)
	{
		Element targetElement = widget.asWidget().getElement();
		smToolTip tip = m_tipMap.get(targetElement);
		
		if( tip != null )
		{
			m_toolTipManager.endTip(tip);
			m_tipMap.remove(targetElement);
		}
	}
}
