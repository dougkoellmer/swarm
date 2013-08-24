package swarm.client.ui.tabs;

import swarm.client.ui.bhI_UIElement;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhStateEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class bhBookmarkTab extends AbsolutePanel implements bhI_TabContent
{
	public bhBookmarkTab()
	{
		this.getElement().getStyle().setBackgroundColor("#ffffff");
		
		this.addStyleName("sm_bookmark_viewer");
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
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
