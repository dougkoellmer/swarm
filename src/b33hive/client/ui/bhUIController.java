package b33hive.client.ui;

import java.util.ArrayList;

import b33hive.client.input.bhMouse;
import b33hive.client.navigation.bhMasterNavigator;
import b33hive.client.navigation.bhMouseNavigator;
import b33hive.client.states.StateContainer_Base;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.ui.cell.bhVisualCellContainer;
import b33hive.client.ui.cell.bhVisualCellFocuser;
import b33hive.client.ui.cell.bhVisualCellHighlight;
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
public class bhUIController extends Object implements bhI_StateEventListener
{
	private static final bhUIController s_instance = new bhUIController();
	
	private final ArrayList<bhI_UIElement> m_listeners = new ArrayList<bhI_UIElement>();
	
	private static void startUpInitialUI()
	{
		s_instance.m_listeners.add(new bhDialogManager(RootPanel.get()));
		s_instance.m_listeners.add(new bhInitialSyncScreen());
	}
	
	private static void shutDownInitialUI()
	{
		s_instance.m_listeners.remove(s_instance.m_listeners.size()-1);
	}
	
	private static void startUpCoreUI()
	{
		bhSplitPanel splitPanel = bhSplitPanel.getInstance();
		bhVisualCellContainer cellContainer = splitPanel.getCellContainer();
		
		bhMouse mouse = new bhMouse(cellContainer.getMouseEnabledLayer());
		
		// TODO: Clean this up so that all this crap doesn't add itself to parent containers in constructors.
		s_instance.m_listeners.add(new bhMasterNavigator(mouse));
		s_instance.m_listeners.add(new bhVisualCellManager(cellContainer.getCellContainerLayer()));
		s_instance.m_listeners.add(splitPanel);
		s_instance.m_listeners.add(new bhVisualCellHighlight(cellContainer.getCellContainerLayer()));
		s_instance.m_listeners.add(new bhVisualCellFocuser(cellContainer.getCellContainerLayer()));
		s_instance.m_listeners.add(new bhVisualCellHud((Panel)cellContainer));
		
		RootLayoutPanel.get().add(splitPanel);
	}
	
	public static bhUIController getInstance()
	{
		return s_instance;
	}
	
	public bhUIController()
	{
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
				bhToolTipManager.getInstance().update(event.getState().getLastTimeStep());
			}
		}
		
		for ( int i = 0; i < m_listeners.size(); i++ )
		{
			m_listeners.get(i).onStateEvent(event);
		}
	}
}