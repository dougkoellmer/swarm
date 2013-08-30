package swarm.client.states.camera;

import swarm.client.entities.smBufferCell;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smPoint;

public class State_GettingMapping extends smA_State
{
	static class Constructor extends smA_StateConstructor
	{
		private final smCellAddress m_address;
		
		public Constructor(smCellAddress address)
		{
			m_address = address;
		}
	}
	
	private smCellAddress m_address = null;
	
	public State_GettingMapping()
	{
		smA_Action.register(new Event_GettingMapping_OnResponse());
	}
	
	smCellAddress getAddress()
	{
		return m_address;
	}
	
	void updateAddress(smCellAddress address)
	{
		m_address = address;
	}
	
	@Override
	public boolean isTransparent()
	{
		return true;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		Constructor thisConstructor = (Constructor) constructor;
		updateAddress(thisConstructor.m_address);
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		m_address = null;
	}
}


