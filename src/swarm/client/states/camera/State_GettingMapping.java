package swarm.client.states.camera;

import swarm.client.entities.bhBufferCell;
import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_EventAction;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhPoint;

public class State_GettingMapping extends bhA_State
{
	static class Constructor extends bhA_StateConstructor
	{
		private final bhCellAddress m_address;
		
		public Constructor(bhCellAddress address)
		{
			m_address = address;
		}
	}
	
	public static class OnResponse extends bhA_EventAction
	{
		public static enum E_Type
		{
			ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
		}
		
		public static class Args extends bhA_ActionArgs
		{
			private final bhCellAddress m_address;
			private final bhCellAddressMapping m_mapping;
			private final E_Type m_responseType;
			
			Args(E_Type responseType, bhCellAddress address, bhCellAddressMapping mapping )
			{
				m_responseType = responseType;
				m_address = address;
				m_mapping = mapping;
			}

			public bhCellAddress getAddress()
			{
				return m_address;
			}
			
			public bhCellAddressMapping getMapping()
			{
				return m_mapping;
			}
			
			public E_Type getType()
			{
				return m_responseType;
			}
		}
		
		@Override
		public boolean isPerformableInBackground()
		{
			return true;
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_GettingMapping.class;
		}
	}
	
	private bhCellAddress m_address = null;
	
	public State_GettingMapping()
	{
		bhA_Action.register(new OnResponse());
	}
	
	bhCellAddress getAddress()
	{
		return m_address;
	}
	
	void updateAddress(bhCellAddress address)
	{
		m_address = address;
	}
	
	@Override
	public boolean isTransparent()
	{
		return true;
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		Constructor thisConstructor = (Constructor) constructor;
		updateAddress(thisConstructor.m_address);
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void willBackground(Class<? extends bhA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		m_address = null;
	}
}


