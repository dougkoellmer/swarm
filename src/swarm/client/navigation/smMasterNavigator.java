package swarm.client.navigation;


import swarm.client.input.smMouse;
import swarm.client.ui.smI_UIElement;
import swarm.shared.statemachine.smStateEvent;

public class smMasterNavigator implements smI_UIElement
{
	private final smMouseNavigator m_mouseNavigator;
	private final smBrowserNavigator m_browserNavigator;
	
	public smMasterNavigator(smMouse mouse, String defaultPageTitle, double floatingHistoryStateFreq_seconds)
	{
		m_mouseNavigator = new smMouseNavigator(mouse);
		m_browserNavigator = new smBrowserNavigator(defaultPageTitle, floatingHistoryStateFreq_seconds);
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		m_mouseNavigator.onStateEvent(event);
		m_browserNavigator.onStateEvent(event);
	}
	
	public smBrowserNavigator getBrowserNavigator()
	{
		return m_browserNavigator;
	}
	
	public smMouseNavigator getMouseNavigator()
	{
		return m_mouseNavigator;
	}
}
