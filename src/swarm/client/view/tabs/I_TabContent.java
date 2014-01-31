package swarm.client.view.tabs;

import swarm.client.view.I_UIElement;

import com.google.gwt.user.client.ui.IsWidget;

public interface I_TabContent extends I_UIElement, IsWidget
{
	void onResize();
	
	void onSelect();
}
