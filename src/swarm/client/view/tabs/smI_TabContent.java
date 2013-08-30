package swarm.client.view.tabs;

import swarm.client.view.smI_UIElement;

import com.google.gwt.user.client.ui.IsWidget;

public interface smI_TabContent extends smI_UIElement, IsWidget
{
	void onResize();
	
	void onSelect();
}
