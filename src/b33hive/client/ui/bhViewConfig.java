package b33hive.client.ui;

import b33hive.client.ui.tabs.bhI_Tab;
import b33hive.shared.statemachine.bhI_StateEventListener;

public class bhViewConfig
{
	public double magFadeInTime_seconds;
	public int magnifierTickCount;
	public String defaultPageTitle;
	public bhI_StateEventListener stateEventListener;
	
	public bhI_Tab[] tabs;
}
