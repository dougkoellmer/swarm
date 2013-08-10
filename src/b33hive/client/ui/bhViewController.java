package b33hive.client.ui;

import java.util.ArrayList;

import b33hive.client.app.bhClientAppConfig;
import b33hive.client.app.bh_c;
import b33hive.client.input.bhMouse;
import b33hive.client.navigation.bhMasterNavigator;
import b33hive.client.navigation.bhMouseNavigator;
import b33hive.client.states.StateContainer_Base;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.ui.cell.bhVisualCellContainer;
import b33hive.client.ui.cell.bhVisualCellFocuser;
import b33hive.client.ui.cell.bhVisualCellHud;
import b33hive.client.ui.cell.bhVisualCellManager;
import b33hive.client.ui.dialog.bhDialogManager;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhE_StateEventType;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
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
		bh_view.splitPanel = new bhSplitPanel(m_viewConfig);
		bhVisualCellContainer cellContainer = bh_view.splitPanel.getCellContainer();
		
		bhMouse mouse = new bhMouse(cellContainer.getMouseEnabledLayer());
		
		// TODO: Clean this up so that all this crap doesn't add itself to parent containers in constructors.
		m_listeners.add(bh_c.navigator = new bhMasterNavigator(mouse, m_appConfig.floatingHistoryUpdateFreq_seconds));
		m_listeners.add(bh_view.cellMngr = new bhVisualCellManager(cellContainer.getCellContainerLayer()));
		m_listeners.add(bh_view.splitPanel);
		//m_listeners.add(new bhVisualCellHighlight(cellContainer.getCellContainerLayer()));
		m_listeners.add(new bhVisualCellFocuser(cellContainer.getCellContainerLayer()));
		//m_listeners.add(new bhVisualCellHud((Panel)cellContainer, m_appConfig));
		
		RootLayoutPanel.get().add(bh_view.splitPanel);
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
				bh_c.toolTipMngr.update(event.getState().getLastTimeStep());
			}
		}
		
		for ( int i = 0; i < m_listeners.size(); i++ )
		{
			m_listeners.get(i).onStateEvent(event);
		}
	}
}