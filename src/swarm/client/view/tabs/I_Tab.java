package swarm.client.view.tabs;

import swarm.shared.statemachine.A_BaseStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface I_Tab
{
	String getName();
	String getToolTipText();
	I_TabContent getContent();
	
	void onAttached(IsWidget tabButton);
	void onStateEvent(A_BaseStateEvent event);
}
