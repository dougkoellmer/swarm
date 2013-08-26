package swarm.client.states.camera;

import swarm.client.states.camera.StateMachine_Camera.PendingSnap;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.client.managers.*;

class smCellAddressManagerListener implements smCellAddressManager.I_Listener
{
	private final StateMachine_Camera m_machine;
	
	smCellAddressManagerListener(StateMachine_Camera machine)
	{
		m_machine = machine;
	}
	
	private boolean dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type type, smCellAddress address, smCellAddressMapping mapping)
	{
		smA_State currentState = m_machine.getCurrentState();
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
	
	private void dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type type, smCellAddress address, smCellAddressMapping mapping)
	{
		boolean dispatch = false;
		
		smA_State currentState = m_machine.getCurrentState();
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
	public void onMappingFound(smCellAddress address, smCellAddressMapping mapping)
	{
		if( dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_FOUND, address, mapping) )
		{
			m_machine.snapToCoordinate(address, mapping.getCoordinate());
		}
	}

	@Override
	public void onMappingNotFound(smCellAddress address)
	{
		dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_NOT_FOUND, address, null);
	}

	@Override
	public void onResponseError(smCellAddress address)
	{
		dispatchGotMappingEvent(State_GettingMapping.OnResponse.E_Type.ON_RESPONSE_ERROR, address, null);
	}
	
	@Override
	public void onAddressFound(smCellAddressMapping mapping, smCellAddress address)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_FOUND, address, mapping);
	}

	@Override
	public void onAddressNotFound(smCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_NOT_FOUND, null, mapping);
	}

	@Override
	public void onResponseError(smCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(StateMachine_Camera.OnAddressResponse.E_Type.ON_RESPONSE_ERROR, null, mapping);
	}
}
