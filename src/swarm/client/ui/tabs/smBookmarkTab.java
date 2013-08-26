package swarm.client.ui.tabs;

import swarm.client.ui.smI_UIElement;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class smBookmarkTab extends AbsolutePanel implements smI_TabContent
{
	public smBookmarkTab()
	{
		this.getElement().getStyle().setBackgroundColor("#ffffff");
		
		this.addStyleName("sm_bookmark_viewer");
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
	}

	@Override
	public void onResize()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelect()
	{
		// TODO Auto-generated method stub
		
	}
}
