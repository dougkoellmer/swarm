package swarm.client.view;

import java.util.ArrayList;

import swarm.client.app.ClientAppConfig;
import swarm.client.app.AppContext;
import swarm.client.input.ClickManager;
import swarm.client.input.Mouse;
import swarm.client.navigation.BrowserNavigator;
import swarm.client.navigation.MouseNavigator;
import swarm.client.navigation.ScrollNavigator;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.view.cell.AlertManager;
import swarm.client.view.cell.VisualCellContainer;
import swarm.client.view.cell.VisualCellFocuser;
import swarm.client.view.cell.VisualCellHighlight;
import swarm.client.view.cell.VisualCellHud;
import swarm.client.view.cell.VisualCellManager;
import swarm.client.view.dialog.DialogManager;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.E_Event;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateContext;
import swarm.shared.statemachine.StateEvent;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * ...
 * @author 
 */
public class ViewController extends Object implements I_StateEventListener
{	
	private final ArrayList<I_StateEventListener> m_listeners = new ArrayList<I_StateEventListener>();
	
	protected final ViewConfig m_viewConfig;
	protected final ClientAppConfig m_appConfig;
	protected final ViewContext m_viewContext;
	
	public ViewController(ViewContext viewContext, ViewConfig config, ClientAppConfig appConfig)
	{
		m_viewContext = viewContext;
		m_viewConfig = config;
		m_appConfig = appConfig;
	}
	
	private void startUpInitialUI()
	{
		m_listeners.add(new DialogManager(m_viewContext.stateContext, m_viewContext.clickMngr, RootPanel.get()));
		m_listeners.add(new InitialSyncScreen());
	}
	
	private void shutDownInitialUI()
	{
		m_listeners.remove(m_listeners.size()-1);
	}
	
	protected void startUpCoreUI()
	{
		m_viewContext.splitPanel = new SplitPanel(m_viewContext, m_viewConfig);
		VisualCellContainer cellContainer = m_viewContext.splitPanel.getCellContainer();
		
		Mouse mouse = new Mouse(cellContainer.getMouseEnabledLayer());
		
		m_viewContext.alertMngr = new AlertManager();
		m_viewContext.clickMngr = new ClickManager();
		m_viewContext.consoleBlocker = new ConsoleBlocker();
		m_viewContext.mouseNavigator = new MouseNavigator(m_viewContext, mouse);
		m_viewContext.browserNavigator = new BrowserNavigator(m_viewContext, m_viewConfig.defaultPageTitle, m_appConfig.floatingHistoryUpdateFreq_seconds);
		m_viewContext.scrollNavigator = new ScrollNavigator(m_viewContext, cellContainer.getScrollContainer(), cellContainer.getCellContainerInner(), cellContainer.getMouseEnabledLayer());
		VisualCellFocuser focuser = new VisualCellFocuser(m_viewContext);
		m_viewContext.cellMngr = new VisualCellManager(m_viewContext, cellContainer.getCellContainerInner());
		VisualCellHighlight highlighter = new VisualCellHighlight(m_viewContext);
		
		addStateListener(m_viewContext.mouseNavigator);
		addStateListener(m_viewContext.browserNavigator); 
		addStateListener(m_viewContext.scrollNavigator);
		addStateListener(m_viewContext.cellMngr);
		addStateListener(m_viewContext.splitPanel);
		
		addStateListener(highlighter);
		addStateListener(focuser);
		//addStateListener(new smVisualCellHud((Panel)cellContainer, m_appConfig));
		
		RootLayoutPanel.get().add(m_viewContext.splitPanel);
		cellContainer.getCellContainerInner().add(focuser);
		cellContainer.getCellContainerInner().add(highlighter);
	}
	
	protected void addStateListener(I_StateEventListener listener)
	{
		m_listeners.add(listener);
	}
	
	public void onStateEvent(StateEvent event)
	{
		if ( event.getType() == E_Event.DID_ENTER )
		{
			if ( event.getState() instanceof StateMachine_Base )
			{
				startUpInitialUI();
			}
			else if ( event.getState() instanceof StateContainer_Base )
			{
				shutDownInitialUI();
				
				startUpCoreUI();
			}
			else if( event.getState() instanceof StateMachine_Camera )
			{
				//--- This is handled hackishly here, instead of internally by split panel, because of a tricky race condition.
				//--- Browser navigator should receive events before split panel, but during the start up sequence, if there's a history
				//--- state available, browser navigator expects the camera viewport to be set already, which split panel could only do afterwards.
				//--- So, bending the rules here a little.
				m_viewContext.splitPanel.setInitialCameraViewport();
			}
		}
		else if( event.getType() == E_Event.DID_UPDATE )
		{
			if( event.getState() instanceof StateMachine_Base )
			{
				m_viewContext.toolTipMngr.update(event.getState().getLastTimeStep());
			}
		}
		
		for ( int i = 0; i < m_listeners.size(); i++ )
		{
			m_listeners.get(i).onStateEvent(event);
		}
	}
}