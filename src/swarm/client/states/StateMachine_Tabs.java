package swarm.client.states;

import java.util.ArrayList;
import java.util.List;

import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smA_StateConstructor;

/**
 * ...
 * @author 
 */
public class StateMachine_Tabs extends smA_StateMachine
{	
	public static class SelectTab extends smA_Action 
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
			int tabIndex = ((Args) args).m_index;
			StateMachine_Tabs tabController = (StateMachine_Tabs) this.getState();
			tabController.setTab(tabIndex);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			int tabIndex = ((Args) args).m_index;
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
	
	private final List<Class<? extends smA_State>> m_tabStates;
	
	private int m_tabIndex = -1;
	
	public StateMachine_Tabs(List<Class<? extends smA_State>> tabStates) 
	{
		smA_Action.register(new SelectTab());
		
		m_tabStates = tabStates;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		
	}
	
	public int calcTabIndex(Class<? extends smA_State> tabState)
	{
		for( int i = 0; i < m_tabStates.size(); i++ )
		{
			if( tabState == m_tabStates.get(i) )
			{
				return i;
			}
		}
		
		return -1;
	}
	
	
	
	int getTab()
	{
		return m_tabIndex;
	}
	
	void setTab(int index)
	{
		if( index == m_tabIndex )
		{
			return;
		}
		
		m_tabIndex = index;
		
		machine_setState(this, m_tabStates.get(m_tabIndex));
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( this.m_tabIndex == -1 )
		{
			this.setTab(0); // just makes sure first tab state is foregrounded on this state's first foreground.
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
	}
}
