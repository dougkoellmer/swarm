package swarm.client.view;

import swarm.client.view.tabs.I_Tab;
import swarm.shared.statemachine.I_StateEventListener;

public class ViewConfig
{
	public double magFadeInTime_seconds = .5;
	public double hudFadeOutTime_seconds = magFadeInTime_seconds;
	public double focuserFadeOutTime_seconds = .25;
	public double focuserMaxAlpha = .91;
	public int magnifierTickCount;
	public String defaultPageTitle;
	public String cellHighlightColor = S_UI.HIGHLIGHT_COLOR;
	public int cellHighlightMinSize = S_UI.HIGHLIGHT_MIN_SIZE;
	public int cellHighlightMaxSize = S_UI.HIGHLIGHT_MAX_SIZE;
	public double initialBumpDistance = 128;
	public double extraScrollArea = 50;
	public double cellSizeChangeTime_seconds = 1;
	public double cellRetractionEasing = 4;
	
	public I_Tab[] tabs;
}
