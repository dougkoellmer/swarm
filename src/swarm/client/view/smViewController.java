package swarm.client.view;

import java.util.ArrayList;

import swarm.client.app.smClientAppConfig;
import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.input.smMouse;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.navigation.smMouseNavigator;
import swarm.client.navigation.smScrollNavigator;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.view.cell.smAlertManager;
import swarm.client.view.cell.smVisualCellContainer;
import swarm.client.view.cell.smVisualCellFocuser;
import swarm.client.view.cell.smVisualCellHighlight;
import swarm.client.view.cell.smVisualCellHud;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.dialog.smDialogManager;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smE_StateEventType;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateContext;
import swarm.shared.statemachine.smStateEvent;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * ...
 * @author 
 */
public class smViewController extends Object implements smI_StateEventListener
{	
	private final ArrayList<smI_StateEventListener> m_listeners = new ArrayList<smI_StateEventListener>();
	
	protected final smViewConfig m_viewConfig;
	protected final smClientAppConfig m_appConfig;
	protected final smViewContext m_viewContext;
	
	public smViewController(smViewContext viewContext, smViewConfig config, smClientAppConfig appConfig)
	{
		m_viewContext = viewContext;
		m_viewConfig = config;
		m_appConfig = appConfig;
	}
	
	private void startUpInitialUI()
	{
		m_listeners.add(new smDialogManager(m_viewContext.stateContext, m_viewContext.clickMngr, RootPanel.get()));
		m_listeners.add(new smInitialSyncScreen());
	}
	
	private void shutDownInitialUI()
	{
		m_listeners.remove(m_listeners.size()-1);
	}
	
	protected void startUpCoreUI()
	{
		m_viewContext.splitPanel = new smSplitPanel(m_viewContext, m_viewConfig);
		smVisualCellContainer cellContainer = m_viewContext.splitPanel.getCellContainer();
		
		smMouse mouse = new smMouse(cellContainer.getMouseEnabledLayer());
		
		m_viewContext.alertMngr = new smAlertManager();
		m_viewContext.clickMngr = new smClickManager();
		m_viewContext.consoleBlocker = new smConsoleBlocker();
		m_viewContext.mouseNavigator = new smMouseNavigator(m_viewContext, mouse);
		m_viewContext.browserNavigator = new smBrowserNavigator(m_viewContext.stateContext, m_viewContext.appContext.cameraMngr, m_viewContext.appContext.jsonFactory, m_viewConfig.defaultPageTitle, m_appConfig.floatingHistoryUpdateFreq_seconds);
		m_viewContext.scrollNavigator = new smScrollNavigator(m_viewContext, cellContainer.getScrollContainer(), cellContainer.getCellContainerInner(), cellContainer.getMouseEnabledLayer());
		smVisualCellFocuser focuser = new smVisualCellFocuser(m_viewContext.stateContext, m_viewContext.appContext);
		m_viewContext.cellMngr = new smVisualCellManager(m_viewContext, cellContainer.getCellContainerInner());
		smVisualCellHighlight highlighter = new smVisualCellHighlight(m_viewContext);
		

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
	
	protected void addStateListener(smI_StateEventListener listener)
	{
		m_listeners.add(listener);
	}
	
	public void onStateEvent(smStateEvent event)
	{
		if ( event.getType() == smE_StateEventType.DID_ENTER )
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
		else if( event.getType() == smE_StateEventType.DID_UPDATE )
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