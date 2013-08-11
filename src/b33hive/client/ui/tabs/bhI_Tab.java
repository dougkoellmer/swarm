package b33hive.client.ui.tabs;

import b33hive.shared.statemachine.bhStateEvent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface bhI_Tab
{
	String getName();
	String getToolTipText();
	bhI_TabContent getContent();
	
	void onAttached(IsWidget tabButton);
	void onStateEvent(bhStateEvent event);
}
