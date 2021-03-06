package swarm.client.view;

import swarm.client.states.State_GenericDialog;
import swarm.client.states.State_Initializing;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.A_BaseStateEvent;
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

public class InitialSyncScreen implements I_UIElement
{
	private final Element m_screenElement;
	private final Element m_labelElement;
	
	public InitialSyncScreen()
	{
		m_screenElement = DOM.getElementById("sm_initial_sync_screen");
		m_labelElement = DOM.getElementById("sm_initial_sync_screen_label");
	}

	@Override
	public void onStateEvent(A_BaseStateEvent event)
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
