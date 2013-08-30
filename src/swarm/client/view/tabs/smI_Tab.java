package swarm.client.view.tabs;

import swarm.shared.statemachine.smStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface smI_Tab
{
	String getName();
	String getToolTipText();
	smI_TabContent getContent();
	
	void onAttached(IsWidget tabButton);
	void onStateEvent(smStateEvent event);
}
