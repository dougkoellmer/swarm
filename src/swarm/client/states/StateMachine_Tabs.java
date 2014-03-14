package swarm.client.states;

import java.util.ArrayList;
import java.util.List;

import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.StateArgs;

/**
 * ...
 * @author 
 */
public class StateMachine_Tabs extends A_StateMachine
{	
	private final List<Class<? extends A_State>> m_tabStates;
	
	private int m_tabIndex = -1;
	
	public StateMachine_Tabs(List<Class<? extends A_State>> tabStates) 
	{
		registerAction(new Action_Tabs_SelectTab());
		
		m_tabStates = tabStates;
	}
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
		
	}
	
	public int calcTabIndex(Class<? extends A_State> tabState)
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
		
		setState(this, m_tabStates.get(m_tabIndex));
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
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
