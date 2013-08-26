package swarm.client.states.camera;

import swarm.client.entities.smBufferCell;
import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_EventAction;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
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
	
	public static class OnResponse extends smA_EventAction
	{
		public static enum E_Type
		{
			ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
		}
		
		public static class Args extends smA_ActionArgs
		{
			private final smCellAddress m_address;
			private final smCellAddressMapping m_mapping;
			private final E_Type m_responseType;
			
			Args(E_Type responseType, smCellAddress address, smCellAddressMapping mapping )
			{
				m_responseType = responseType;
				m_address = address;
				m_mapping = mapping;
			}

			public smCellAddress getAddress()
			{
				return m_address;
			}
			
			public smCellAddressMapping getMapping()
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
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_GettingMapping.class;
		}
	}
	
	private smCellAddress m_address = null;
	
	public State_GettingMapping()
	{
		smA_Action.register(new OnResponse());
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


