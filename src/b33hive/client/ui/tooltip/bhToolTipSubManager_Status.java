package b33hive.client.ui.tooltip;

import java.util.HashMap;

import b33hive.client.ui.bhE_ZIndex;
import b33hive.shared.debugging.bhU_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

class bhToolTipSubManager_Status implements bhI_ToolTipSubManager
{
	private final bhToolTipManager m_toolTipManager;
	
	private final HashMap<Element, bhToolTip> m_tipMap = new HashMap<Element, bhToolTip>();
	
	bhToolTipSubManager_Status(bhToolTipManager toolTipManager)
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
	public void addTip(IsWidget widget, bhToolTipConfig config)
	{
		Element targetElement = widget.asWidget().getElement();
		bhToolTip toolTip = new bhToolTip();
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
		bhToolTip tip = m_tipMap.get(targetElement);
		
		if( tip != null )
		{
			m_toolTipManager.endTip(tip);
			m_tipMap.remove(targetElement);
		}
	}
}
