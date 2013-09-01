package swarm.client.view.tooltip;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.IsWidget;

public interface smI_ToolTipSubManager
{
	void onExternalMouseUp(MouseUpEvent event);
	
	void onGlobalMouseDown(MouseDownEvent event);
	
	void addTip(IsWidget widget, smToolTipConfig config);
	
	void removeTip(IsWidget widget);
	
	void update(double timeStep);
}