package swarm.client.view.tabs;

import swarm.shared.statemachine.A_BaseStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class A_Tab implements I_Tab
{
	protected IsWidget m_tabButton;
	
	private final String m_name;
	private final String m_toolTipText;
	private final I_TabContent m_content;
	
	public A_Tab(String name, String toolTipText, I_TabContent content)
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
	public I_TabContent getContent()
	{
		return m_content;
	}

	@Override
	public void onAttached(IsWidget tabButton)
	{
		m_tabButton = tabButton;
	}

	@Override
	public void onStateEvent(A_BaseStateEvent event)
	{
		m_content.onStateEvent(event);
	}
}
