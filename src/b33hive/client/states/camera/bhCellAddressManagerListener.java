package com.b33hive.client.states.camera;

import com.b33hive.client.states.camera.StateMachine_Camera.PendingSnap;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.client.managers.*;

class bhCellAddressManagerListener implements bhCellAddressManager.I_Listener
{
	private final StateMachine_Camera m_machine;
	
	bhCellAddressManagerListener(StateMachine_Camera machine)
	{
		m_machine = machine;
	}
	
	private boolean dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type type, bhCellAddress address, bhCellAddressMapping mapping)
	{
		bhA_State currentState = m_machine.getCurrentState();
		if( currentState instanceof State_GettingMapping )
		{
			State_GettingMapping gettingAddy = ((State_GettingMapping)currentState);
			if( gettingAddy.getAddress().isEqualTo(address) )
			{
				State_GettingMapping.OnResponse.Args args = new State_GettingMapping.OnResponse.Args(type, address, mapping);
				
				currentState.performAction(State_GettingMapping.OnResponse.class, args);
				
				m_machine.tryPoppingGettingAddressState();
				
				return true;
			}
		}
		
		return false;
	}
	
	private void dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type type, bhCellAddress address, bhCellAddressMapping mapping)
	{
		boolean dispatch = false;
		
		bhA_State currentState = m_machine.getCurrentState();
		if( currentState instanceof State_CameraSnapping )
		{
			State_CameraSnapping snappingState = m_machine.getCurrentState();
			
			if( snappingState.getTargetCoordinate().isEqualTo(mapping.getCoordinate()) )
			{
				dispatch = true;
			}
		}
		else if( currentState instanceof State_ViewingCell )
		{
			State_ViewingCell viewingState = m_machine.getCurrentState();
			
			if( viewingState.getCell().getCoordinate().isEqualTo(mapping.getCoordinate()) )
			{
				dispatch = true;
			}
		}
		
		if( dispatch )
		{
			StateMachine_Camera.OnAddressResponse.Args args = new StateMachine_Camera.OnAddressResponse.Args(type, address, mapping);
			
			m_machine.performAction(StateMachine_Camera.OnAddressResponse.class, args);
		}
	}
	
	@Override
	public void onMappingFound(bhCellAddress address, bhCellAddressMapping mapping)
	{
		if( dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_FOUND, address, mapping) )
		{
			m_machine.snapToCoordinate(address, mapping.getCoordinate());
		}
	}

	@Override
	public void onMappingNotFound(bhCellAddress address)
	{
		dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_NOT_FOUND, address, null);
	}

	@Override
	public void onResponseError(bhCellAddress address)
	{
		dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_RESPONSE_ERROR, address, null);
	}
	
	@Override
	public void onAddressFound(bhCellAddressMapping mapping, bhCellAddress address)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_FOUND, address, mapping);
	}

	@Override
	public void onAddressNotFound(bhCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_NOT_FOUND, null, mapping);
	}

	@Override
	public void onResponseError(bhCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_RESPONSE_ERROR, null, mapping);
	}
}
