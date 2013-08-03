package b33hive.client.ui.tabs;

import b33hive.client.ui.bhI_UIElement;
import com.google.gwt.user.client.ui.IsWidget;

public interface bhI_TabContent extends bhI_UIElement, IsWidget
{
	void onResize();
	
	void onSelect();
}
