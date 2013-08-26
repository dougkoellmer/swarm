package swarm.client.ui;

import swarm.client.states.State_GenericDialog;
import swarm.client.states.State_Initializing;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class smInitialSyncScreen implements smI_UIElement
{
	private final Element m_screenElement;
	private final Element m_labelElement;
	
	public smInitialSyncScreen()
	{
		m_screenElement = DOM.getElementById("sm_initial_sync_screen");
		m_labelElement = DOM.getElementById("sm_initial_sync_screen_label");
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_Initializing )
				{
				}
				else if( event.getState() instanceof State_GenericDialog )
				{
					//--- DRK > TODO: We're assuming this is an error, which for now it will always be, but maybe not always.
					m_labelElement.setInnerHTML(":-(");
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_Initializing )
				{
					this.m_screenElement.removeFromParent();
				}
				
				break;
			}
		}
	}
}
