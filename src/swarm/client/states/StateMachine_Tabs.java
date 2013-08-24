package swarm.client.states;

import java.util.ArrayList;
import java.util.List;

import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.shared.statemachine.bhA_Action;

import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateMachine;
import swarm.shared.statemachine.bhA_StateConstructor;

/**
 * ...
 * @author 
 */
public class StateMachine_Tabs extends bhA_StateMachine
{	
	public static class SelectTab extends bhA_Action 
	{
		public static class Args extends bhA_ActionArgs
		{
			private int m_index;
			
			public void setIndex(int index)
			{
				m_index = index;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			int tabIndex = ((Args) args).m_index;
			StateMachine_Tabs tabController = (StateMachine_Tabs) this.getState();
			tabController.setTab(tabIndex);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
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
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Tabs.class;
		}
	}
	
	private final List<Class<? extends bhA_State>> m_tabStates;
	
	private int m_tabIndex = -1;
	
	public StateMachine_Tabs(List<Class<? extends bhA_State>> tabStates) 
	{
		bhA_Action.register(new SelectTab());
		
		m_tabStates = tabStates;
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		
	}
	
	public int calcTabIndex(Class<? extends bhA_State> tabState)
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
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
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
