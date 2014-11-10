package swarm.client.view.tabs;

import swarm.client.view.I_UIElement;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_BaseStateEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class BookmarkTab extends AbsolutePanel implements I_TabContent
{
	public BookmarkTab()
	{
		this.getElement().getStyle().setBackgroundColor("#ffffff");
		
		this.addStyleName("sm_bookmark_viewer");
	}
	
	@Override
	public void onStateEvent(A_BaseStateEvent event)
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
