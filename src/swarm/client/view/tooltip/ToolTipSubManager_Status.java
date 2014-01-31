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

class ToolTipSubManager_Status implements I_ToolTipSubManager
{
	private final ToolTipManager m_toolTipManager;
	
	private final HashMap<Element, ToolTip> m_tipMap = new HashMap<Element, ToolTip>();
	
	ToolTipSubManager_Status(ToolTipManager toolTipManager)
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
	public void addTip(IsWidget widget, ToolTipConfig config)
	{
		Element targetElement = widget.asWidget().getElement();
		ToolTip toolTip = new ToolTip();
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
		ToolTip tip = m_tipMap.get(targetElement);
		
		if( tip != null )
		{
			m_toolTipManager.endTip(tip);
			m_tipMap.remove(targetElement);
		}
	}
}
