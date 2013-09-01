package swarm.client.view.dialog;

import swarm.client.input.smClickManager;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.view.smE_ZIndex;
import swarm.client.view.smI_UIElement;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

public class smDialogManager implements smI_UIElement
{
	private final Panel m_outerContainer;
	private FocusPanel m_focusPanel = null;//new FocusPanel();
	private AbsolutePanel m_innerContainer = null;//new AbsolutePanel();
	private smDialog m_dialog = null;//new smDialog();
	
	private FlowPanel m_dialogGlass = null;//new FlowPanel();
	private HorizontalPanel m_dialogContainer = null;//new HorizontalPanel();
	
	private boolean m_initialized = false;
	
	private final smClickManager m_clickMngr;
	
	public smDialogManager(smClickManager clickMngr, Panel outerContainer)
	{
		m_outerContainer = outerContainer;
		m_clickMngr = clickMngr;
	}
	
	private void init()
	{
		if( m_initialized )  return;
		
		m_focusPanel = new FocusPanel();
		m_innerContainer = new AbsolutePanel();
		m_dialog = new smDialog(m_clickMngr, 512, 256, new smDialog.I_Delegate()
		{
			@Override
			public void onOkPressed()
			{
				//--- DRK > TODO: Hopefully in future I'll have a better overridable action set up,
				//---				so that I only have to call State_GenericDialog.Ok, and the system knows
				//---				to call State_AsyncDialog.Ok because it is State_AsyncDialog that is foregrounded.
				//---				Until then, I present the following somewhat sloppy workaround.
				if( smA_Action.performAction(State_GenericDialog.Ok.class) )
				{
				}
				else if( smA_Action.performAction(State_AsyncDialog.Ok.class) )
				{
					
				}
			}
		});
		m_dialogGlass = new FlowPanel();
		m_dialogContainer = new HorizontalPanel();
		
		
		
		m_focusPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_focusPanel.getElement().getStyle().setTop(0, Unit.PX);
		m_focusPanel.getElement().getStyle().setLeft(0, Unit.PX);
		
		m_focusPanel.setSize("100%", "100%");
		m_innerContainer.setSize("100%", "100%");
		m_dialogGlass.addStyleName("sm_dialog_glass");
		m_dialogContainer.addStyleName("sm_dialog_container");
		
		smE_ZIndex.DIALOG_GLASS.assignTo(m_dialogGlass);
		smE_ZIndex.DIALOG.assignTo(m_dialogContainer);
		
		m_focusPanel.add(m_innerContainer);
		
		m_innerContainer.add(m_dialogGlass);
		m_innerContainer.add(m_dialogContainer);

		m_focusPanel.addKeyUpHandler(new KeyUpHandler()
		{
			@Override
			public void onKeyUp(KeyUpEvent event)
			{
				m_dialog.onKeyPressed(event.getNativeKeyCode());
				event.preventDefault();
			}
		});
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_GenericDialog )
				{
					init();
					
					m_outerContainer.add(m_focusPanel);

					m_focusPanel.getElement().focus();
					
					State_GenericDialog dialogState = (State_GenericDialog) event.getState();
					m_dialog.setTitle(dialogState.getTitle());
					
					m_dialog.setBodyHtml(dialogState.getBody());
					
					m_dialogContainer.add(m_dialog);
					m_dialogContainer.setCellHorizontalAlignment(m_dialog, HasHorizontalAlignment.ALIGN_CENTER);
					m_dialogContainer.setCellVerticalAlignment(m_dialog, HasVerticalAlignment.ALIGN_MIDDLE);
				}
				break;
			}
			
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof State_GenericDialog )
				{
					m_outerContainer.remove(m_focusPanel);
					
					m_dialogContainer.remove(m_dialog);
				}
				
				break;
			}
		}
	}
}
