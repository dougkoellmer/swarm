package swarm.client.ui;

import swarm.client.ui.tabs.smI_Tab;
import swarm.shared.statemachine.smI_StateEventListener;

public class smViewConfig
{
	public double magFadeInTime_seconds;
	public int magnifierTickCount;
	public String defaultPageTitle;
	public smI_StateEventListener stateEventListener;
	
	public smI_Tab[] tabs;
}
