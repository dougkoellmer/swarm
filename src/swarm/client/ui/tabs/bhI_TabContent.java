package swarm.client.ui.tabs;

import swarm.client.ui.bhI_UIElement;
import com.google.gwt.user.client.ui.IsWidget;

public interface bhI_TabContent extends bhI_UIElement, IsWidget
{
	void onResize();
	
	void onSelect();
}
