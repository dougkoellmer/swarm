package swarm.client.ui.tabs;

import swarm.client.ui.smI_UIElement;
import com.google.gwt.user.client.ui.IsWidget;

public interface smI_TabContent extends smI_UIElement, IsWidget
{
	void onResize();
	
	void onSelect();
}
