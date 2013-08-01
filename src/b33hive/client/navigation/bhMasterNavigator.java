package com.b33hive.client.navigation;

import com.b33hive.client.input.bhMouse;
import com.b33hive.client.ui.bhI_UIElement;
import com.b33hive.shared.statemachine.bhStateEvent;

public class bhMasterNavigator implements bhI_UIElement
{
	private final bhMouseNavigator m_mouseNavigator;
	private final bhBrowserNavigator m_browserNavigator;
	
	public bhMasterNavigator(bhMouse mouse)
	{
		m_mouseNavigator = new bhMouseNavigator(mouse);
		m_browserNavigator = new bhBrowserNavigator();
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		m_mouseNavigator.onStateEvent(event);
		m_browserNavigator.onStateEvent(event);
	}
}
