package swarm.client.ui;

import java.util.ArrayList;

import swarm.client.app.bhClientAppConfig;
import swarm.client.app.sm_c;
import swarm.client.input.bhMouse;
import swarm.client.navigation.bhMasterNavigator;
import swarm.client.navigation.bhMouseNavigator;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.ui.cell.bhVisualCellContainer;
import swarm.client.ui.cell.bhVisualCellFocuser;
import swarm.client.ui.cell.bhVisualCellHud;
import swarm.client.ui.cell.bhVisualCellManager;
import swarm.client.ui.dialog.bhDialogManager;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhE_StateEventType;
import swarm.shared.statemachine.bhI_StateEventListener;
import swarm.shared.statemachine.bhStateEvent;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * ...
 * @author 
 */
public class bhViewController extends Object implements bhI_StateEventListener
{	
	private final ArrayList<bhI_UIElement> m_listeners = new ArrayList<bhI_UIElement>();
	private final bhViewConfig m_viewConfig;
	private final bhClientAppConfig m_appConfig;
	
	public bhViewController(bhViewConfig config, bhClientAppConfig appConfig)
	{
		m_viewConfig = config;
		m_appConfig = appConfig;
	}
	
	private void startUpInitialUI()
	{
		m_listeners.add(new bhDialogManager(RootPanel.get()));
		m_listeners.add(new bhInitialSyncScreen());
	}
	
	private void shutDownInitialUI()
	{
		m_listeners.remove(m_listeners.size()-1);
	}
	
	protected void startUpCoreUI()
	{
		sm_view.splitPanel = new bhSplitPanel(m_viewConfig);
		bhVisualCellContainer cellContainer = sm_view.splitPanel.getCellContainer();
		
		bhMouse mouse = new bhMouse(cellContainer.getMouseEnabledLayer());
		
		// TODO: Clean this up so that all this crap doesn't add itself to parent containers in constructors.
		m_listeners.add(sm_c.navigator = new bhMasterNavigator(mouse, m_viewConfig.defaultPageTitle, m_appConfig.floatingHistoryUpdateFreq_seconds));
		m_listeners.add(sm_view.cellMngr = new bhVisualCellManager(cellContainer.getCellContainerInner()));
		m_listeners.add(sm_view.splitPanel);
		//m_listeners.add(new bhVisualCellHighlight(cellContainer.getCellContainerLayer()));
		m_listeners.add(new bhVisualCellFocuser(cellContainer.getCellContainerInner()));
		//m_listeners.add(new bhVisualCellHud((Panel)cellContainer, m_appConfig));
		
		RootLayoutPanel.get().add(sm_view.splitPanel);
	}
	
	protected void addStateListener(bhI_UIElement listener)
	{
		m_listeners.add(listener);
	}
	
	public void onStateEvent(bhStateEvent event)
	{
		if ( event.getType() == bhE_StateEventType.DID_ENTER )
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
		else if( event.getType() == bhE_StateEventType.DID_UPDATE )
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