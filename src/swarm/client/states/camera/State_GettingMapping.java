package swarm.client.states.camera;

import swarm.client.entities.BufferCell;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_BaseStateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Point;

public class State_GettingMapping extends A_State
{
	static class Constructor extends StateArgs
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
		register(new Event_GettingMapping_OnResponse());
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
	protected void didEnter()
	{
		Constructor thisConstructor = getArgs();
		updateAddress(thisConstructor.m_address);
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


