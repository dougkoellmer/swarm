package b33hive.client.ui.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.gwt.user.client.Timer;

import b33hive.client.ui.bhE_ZIndex;
import b33hive.client.ui.bhS_UI;
import b33hive.shared.debugging.bhU_Debug;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

class bhToolTipSubManager_Notification implements bhI_ToolTipSubManager
{
	private static class Notification
	{
		private double m_elapsedTime = 0;
		private final bhToolTip m_toolTip;
		
		Notification(bhToolTip toolTip)
		{
			m_toolTip = toolTip;
		}
		
		private void update(double timeStep)
		{
			m_elapsedTime += timeStep;
		}
		
		private boolean isFinished()
		{
			return m_elapsedTime >= bhS_UI.TOOL_TIP_NOTIFICATION_DURATION;
		}
	}
	
	private final bhToolTipManager m_toolTipManager;
	
	private final ArrayList<Notification> m_notifications = new ArrayList<Notification>();
	
	bhToolTipSubManager_Notification(bhToolTipManager toolTipManager)
	{
		m_toolTipManager = toolTipManager;
	}

	@Override
	public void onExternalMouseUp(MouseUpEvent event)
	{
		// NOOP
	}

	@Override
	public void onGlobalMouseDown(MouseDownEvent event)
	{
		Element target = Element.as(event.getNativeEvent().getEventTarget());
		
		removeTip(target);
	}
	
	private void removeTip(Element target)
	{
		for( int i = m_notifications.size()-1 ; i >= 0; i-- )
		{
			if( m_notifications.get(i).m_toolTip.getTargetElement() == target )
			{
				m_toolTipManager.endTip(m_notifications.get(i).m_toolTip);
				m_notifications.remove(i);
			}
		}
	}

	@Override
	public void addTip(IsWidget widget, bhToolTipConfig config)
	{
		final bhToolTip toolTip = new bhToolTip();
		
		m_notifications.add(new Notification(toolTip));
		
		m_toolTipManager.prepareTip(toolTip, widget.asWidget().getElement(), config);
		m_toolTipManager.showTip(toolTip, null);
	}

	@Override
	public void update(double timeStep)
	{
		for( int i = m_notifications.size()-1 ; i >= 0; i-- )
		{
			m_notifications.get(i).update(timeStep);
			
			if( m_notifications.get(i).isFinished() )
			{
				m_toolTipManager.endTip(m_notifications.get(i).m_toolTip);
				m_notifications.remove(i);
			}
		}
	}

	@Override
	public void removeTip(IsWidget widget)
	{
		this.removeTip(widget.asWidget().getElement());
	}
}
