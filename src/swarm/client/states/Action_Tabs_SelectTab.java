package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_Tabs_SelectTab extends A_Action 
{
	public static class Args extends StateArgs
	{
		private int m_index;
		
		public void setIndex(int index)
		{
			m_index = index;
		}
	}
	
	@Override
	public void perform(StateArgs args)
	{
		int tabIndex = ((Action_Tabs_SelectTab.Args) args).m_index;
		StateMachine_Tabs tabController = (StateMachine_Tabs) this.getState();
		tabController.setTab(tabIndex);
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
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
	public Class<? extends A_State> getStateAssociation()
	{
		return StateMachine_Tabs.class;
	}
}