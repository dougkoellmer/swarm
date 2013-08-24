package swarm.client.ui;

import swarm.client.ui.tabs.bhI_Tab;
import swarm.shared.statemachine.bhI_StateEventListener;

public class bhViewConfig
{
	public double magFadeInTime_seconds;
	public int magnifierTickCount;
	public String defaultPageTitle;
	public bhI_StateEventListener stateEventListener;
	
	public bhI_Tab[] tabs;
}
