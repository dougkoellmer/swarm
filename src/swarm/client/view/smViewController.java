package swarm.client.view;

import java.util.ArrayList;

import swarm.client.app.smClientAppConfig;
import swarm.client.app.smAppContext;
import swarm.client.input.smMouse;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.navigation.smMouseNavigator;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.view.cell.smVisualCellContainer;
import swarm.client.view.cell.smVisualCellFocuser;
import swarm.client.view.cell.smVisualCellHud;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.dialog.smDialogManager;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smE_StateEventType;
import swarm.shared.statemachine.smI_StateEventListener;
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
	private final ArrayList<smI_UIElement> m_listeners = new ArrayList<smI_UIElement>();
	private final smViewConfig m_viewConfig;
	private final smClientAppConfig m_appConfig;
	private final smAppContext m_appContext;
	private final smViewContext m_viewContext;
	
	public smViewController(smAppContext appContext, smViewContext viewContext, smViewConfig config, smClientAppConfig appConfig)
	{
		m_viewContext = viewContext;
		m_appContext = appContext;
		m_viewConfig = config;
		m_appConfig = appConfig;
	}
	
	private void startUpInitialUI()
	{
		m_listeners.add(new smDialogManager(RootPanel.get()));
		m_listeners.add(new smInitialSyncScreen());
	}
	
	private void shutDownInitialUI()
	{
		m_listeners.remove(m_listeners.size()-1);
	}
	
	protected void startUpCoreUI()
	{
		m_viewContext.splitPanel = new smSplitPanel(m_appContext, m_viewContext, m_viewConfig);
		smVisualCellContainer cellContainer = m_viewContext.splitPanel.getCellContainer();
		
		smMouse mouse = new smMouse(cellContainer.getMouseEnabledLayer());
		
		smMouseNavigator mouseNavigator = new smMouseNavigator(m_appContext.gridMngr, m_appContext.cameraMngr, mouse);
		smBrowserNavigator browserNavigator = new smBrowserNavigator(m_appContext.cameraMngr, m_appContext.jsonFactory, m_viewConfig.defaultPageTitle, m_appConfig.floatingHistoryUpdateFreq_seconds);
		smVisualCellFocuser focuser = new smVisualCellFocuser(m_appContext);
		//smVisualCellHighlight highlighter = new smVisualCellHighlight(appContext, config, appConfig);
		
		cellContainer.getCellContainerInner().add(focuser);
		//cellContainer.getCellContainerInner().add(highlighter);
		
		// TODO: Clean this up so that all this crap doesn't add itself to parent containers in constructors.
		m_listeners.add(mouseNavigator);
		m_listeners.add(browserNavigator);
		m_listeners.add(smViewContext.cellMngr = new smVisualCellManager(cellContainer.getCellContainerInner()));
		m_listeners.add(m_viewContext.splitPanel);
		//m_listeners.add(highlighter);
		m_listeners.add(focuser);
		//m_listeners.add(new smVisualCellHud((Panel)cellContainer, m_appConfig));
		
		RootLayoutPanel.get().add(m_viewContext.splitPanel);
	}
	
	protected void addStateListener(smI_UIElement listener)
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