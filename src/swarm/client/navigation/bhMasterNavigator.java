package swarm.client.navigation;


import swarm.client.input.bhMouse;
import swarm.client.ui.bhI_UIElement;
import swarm.shared.statemachine.bhStateEvent;

public class bhMasterNavigator implements bhI_UIElement
{
	private final bhMouseNavigator m_mouseNavigator;
	private final bhBrowserNavigator m_browserNavigator;
	
	public bhMasterNavigator(bhMouse mouse, String defaultPageTitle, double floatingHistoryStateFreq_seconds)
	{
		m_mouseNavigator = new bhMouseNavigator(mouse);
		m_browserNavigator = new bhBrowserNavigator(defaultPageTitle, floatingHistoryStateFreq_seconds);
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		m_mouseNavigator.onStateEvent(event);
		m_browserNavigator.onStateEvent(event);
	}
	
	public bhBrowserNavigator getBrowserNavigator()
	{
		return m_browserNavigator;
	}
	
	public bhMouseNavigator getMouseNavigator()
	{
		return m_mouseNavigator;
	}
}
