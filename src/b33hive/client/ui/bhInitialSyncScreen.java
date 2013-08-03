package b33hive.client.ui;

import b33hive.client.states.State_GenericDialog;
import b33hive.client.states.State_Initializing;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
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

public class bhInitialSyncScreen implements bhI_UIElement
{
	private final Element m_screenElement;
	private final Element m_labelElement;
	
	public bhInitialSyncScreen()
	{
		m_screenElement = DOM.getElementById("bh_initial_sync_screen");
		m_labelElement = DOM.getElementById("bh_initial_sync_screen_label");
	}

	@Override
	public void onStateEvent(bhStateEvent event)
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
