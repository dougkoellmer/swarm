package swarm.client.states;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_Tabs_SelectTab extends smA_Action 
{
	public static class Args extends smA_ActionArgs
	{
		private int m_index;
		
		public void setIndex(int index)
		{
			m_index = index;
		}
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		int tabIndex = ((Action_Tabs_SelectTab.Args) args).m_index;
		StateMachine_Tabs tabController = (StateMachine_Tabs) this.getState();
		tabController.setTab(tabIndex);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		int tabIndex = ((Action_Tabs_SelectTab.Args) args).m_index;
		StateMachine_Tabs tabController = (StateMachine_Tabs) this.getState();
		
		if( tabController.getTab() == tabIndex )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return StateMachine_Tabs.class;
	}
}