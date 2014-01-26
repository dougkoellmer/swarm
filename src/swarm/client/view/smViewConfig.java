package swarm.client.view;

import swarm.client.view.tabs.smI_Tab;
import swarm.shared.statemachine.smI_StateEventListener;

public class smViewConfig
{
	public double magFadeInTime_seconds;
	public int magnifierTickCount;
	public String defaultPageTitle;
	public String cellHighlightColor = smS_UI.HIGHLIGHT_COLOR;
	public int cellHighlightMinSize = smS_UI.HIGHLIGHT_MIN_SIZE;
	public int cellHighlightMaxSize = smS_UI.HIGHLIGHT_MAX_SIZE;
	public double initialBumpDistance = 128;
	public double extraScrollArea = 50;
	public smI_Tab[] tabs;
}
