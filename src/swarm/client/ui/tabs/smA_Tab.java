package swarm.client.ui.tabs;

import swarm.shared.statemachine.smStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class smA_Tab implements smI_Tab
{
	protected IsWidget m_tabButton;
	
	private final String m_name;
	private final String m_toolTipText;
	private final smI_TabContent m_content;
	
	public smA_Tab(String name, String toolTipText, smI_TabContent content)
	{
		m_name = name;
		m_toolTipText = toolTipText;
		m_content = content;
	}
	
	@Override
	public String getName()
	{
		return m_name;
	}

	@Override
	public String getToolTipText()
	{
		return m_toolTipText;
	}

	@Override
	public smI_TabContent getContent()
	{
		return m_content;
	}

	@Override
	public void onAttached(IsWidget tabButton)
	{
		m_tabButton = tabButton;
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		m_content.onStateEvent(event);
	}
}
