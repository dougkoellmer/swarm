package com.b33hive.client.states.camera;

import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.client.states.camera.StateMachine_Camera.CameraManager;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_ActionArgs;
import com.b33hive.shared.statemachine.bhA_EventAction;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhPoint;

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


