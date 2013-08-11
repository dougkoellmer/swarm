package b33hive.client.ui.tabs;

import b33hive.shared.statemachine.bhStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class bhA_Tab implements bhI_Tab
{
	protected IsWidget m_tabButton;
	
	private final String m_name;
	private final String m_toolTipText;
	private final bhI_TabContent m_content;
	
	public bhA_Tab(String name, String toolTipText, bhI_TabContent content)
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
	public bhI_TabContent getContent()
	{
		return m_content;
	}

	@Override
	public void onAttached(IsWidget tabButton)
	{
		m_tabButton = tabButton;
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		m_content.onStateEvent(event);
	}
}
