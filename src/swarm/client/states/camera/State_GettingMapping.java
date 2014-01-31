package swarm.client.states.camera;

import swarm.client.entities.BufferCell;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Point;

public class State_GettingMapping extends A_State
{
	static class Constructor extends A_StateConstructor
	{
		private final CellAddress m_address;
		
		public Constructor(CellAddress address)
		{
			m_address = address;
		}
	}
	
	private CellAddress m_address = null;
	
	public State_GettingMapping()
	{
		registerAction(new Event_GettingMapping_OnResponse());
	}
	
	CellAddress getAddress()
	{
		return m_address;
	}
	
	void updateAddress(CellAddress address)
	{
		m_address = address;
	}
	
	@Override
	public boolean isTransparent()
	{
		return true;
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
		Constructor thisConstructor = (Constructor) constructor;
		updateAddress(thisConstructor.m_address);
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void willBackground(Class<? extends A_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		m_address = null;
	}
}


