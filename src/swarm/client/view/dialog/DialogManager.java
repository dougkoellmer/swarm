package swarm.client.view.dialog;

import swarm.client.input.ClickManager;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.view.E_ZIndex;
import swarm.client.view.I_UIElement;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateContext;
import swarm.shared.statemachine.StateEvent;
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

public class DialogManager implements I_UIElement
{
	private final Panel m_outerContainer;
	private FocusPanel m_focusPanel = null;//new FocusPanel();
	private AbsolutePanel m_innerContainer = null;//new AbsolutePanel();
	private Dialog m_dialog = null;//new smDialog();
	
	private FlowPanel m_dialogGlass = null;//new FlowPanel();
	private HorizontalPanel m_dialogContainer = null;//new HorizontalPanel();
	
	private boolean m_initialized = false;
	
	private final ClickManager m_clickMngr;
	private final StateContext m_stateContext;
	
	public DialogManager(StateContext stateContext, ClickManager clickMngr, Panel outerContainer)
	{
		m_stateContext = stateContext;
		m_outerContainer = outerContainer;
		m_clickMngr = clickMngr;
	}
	
	private void init()
	{
		if( m_initialized )  return;
		
		m_focusPanel = new FocusPanel();
		m_innerContainer = new AbsolutePanel();
		m_dialog = new Dialog(m_clickMngr, 512, 256, new Dialog.I_Delegate()
		{
			@Override
			public void onOkPressed()
			{
				//--- DRK > TODO: Hopefully in future I'll have a better overridable action set up,
				//---				so that I only have to call State_GenericDialog.Ok, and the system knows
				//---				to call State_AsyncDialog.Ok because it is State_AsyncDialog that is foregrounded.
				//---				Until then, I present the following somewhat sloppy workaround.
				if( m_stateContext.performAction(State_GenericDialog.Ok.class) )
				{
				}
				else if( m_stateContext.performAction(State_AsyncDialog.Ok.class) )
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
		
		E_ZIndex.DIALOG_GLASS.assignTo(m_dialogGlass);
		E_ZIndex.DIALOG.assignTo(m_dialogContainer);
		
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
	public void onStateEvent(StateEvent event)
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
