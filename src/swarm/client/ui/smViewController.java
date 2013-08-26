package swarm.client.ui;

import java.util.ArrayList;

import swarm.client.app.smClientAppConfig;
import swarm.client.app.sm_c;
import swarm.client.input.smMouse;
import swarm.client.navigation.smMasterNavigator;
import swarm.client.navigation.smMouseNavigator;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.ui.cell.smVisualCellContainer;
import swarm.client.ui.cell.smVisualCellFocuser;
import swarm.client.ui.cell.smVisualCellHud;
import swarm.client.ui.cell.smVisualCellManager;
import swarm.client.ui.dialog.smDialogManager;
import swarm.client.ui.tooltip.smToolTipManager;
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
	
	public smViewController(smViewConfig config, smClientAppConfig appConfig)
	{
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
		sm_view.splitPanel = new smSplitPanel(m_viewConfig);
		bhVisualCellContainer cellContainer = sm_view.splitPanel.getCellContainer();
		
		bhMouse mouse = new smMouse(cellContainer.getMouseEnabledLayer());
		
		// TODO: Clean this up so that all this crap doesn't add itself to parent containers in constructors.
		m_listeners.add(sm_c.navigator = new smMasterNavigator(mouse, m_viewConfig.defaultPageTitle, m_appConfig.floatingHistoryUpdateFreq_seconds));
		m_listeners.add(sm_view.cellMngr = new smVisualCellManager(cellContainer.getCellContainerInner()));
		m_listeners.add(sm_view.splitPanel);
		//m_listeners.add(new smVisualCellHighlight(cellContainer.getCellContainerLayer()));
		m_listeners.add(new smVisualCellFocuser(cellContainer.getCellContainerInner()));
		//m_listeners.add(new smVisualCellHud((Panel)cellContainer, m_appConfig));
		
		RootLayoutPanel.get().add(sm_view.splitPanel);
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
				sm_c.toolTipMngr.update(event.getState().getLastTimeStep());
			}
		}
		
		for ( int i = 0; i < m_listeners.size(); i++ )
		{
			m_listeners.get(i).onStateEvent(event);
		}
	}
}