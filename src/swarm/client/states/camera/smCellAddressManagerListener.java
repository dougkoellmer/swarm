package swarm.client.states.camera;

import swarm.client.states.camera.StateMachine_Camera.PendingSnap;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.client.managers.*;

class smCellAddressManagerListener implements smCellAddressManager.I_Listener
{
	private final StateMachine_Camera m_machine;
	
	private final Event_Camera_OnAddressResponse.Args m_onAddressResponseArgs = new Event_Camera_OnAddressResponse.Args();
	private final Action_Camera_SnapToCoordinate.Args m_snapToCoordArgs = new Action_Camera_SnapToCoordinate.Args();
	
	smCellAddressManagerListener(StateMachine_Camera machine)
	{
		m_machine = machine;
	}
	
	private boolean dispatchGotMappingEvent(Event_GettingMapping_OnResponse.E_Type type, smCellAddress address, smCellAddressMapping mapping)
	{
		smA_State currentState = m_machine.getCurrentState();
		if( currentState instanceof State_GettingMapping )
		{
			State_GettingMapping gettingAddy = ((State_GettingMapping)currentState);
			if( gettingAddy.getAddress().isEqualTo(address) )
			{
				Event_GettingMapping_OnResponse.Args args = new Event_GettingMapping_OnResponse.Args(type, address, mapping);
				
				currentState.performAction(Event_GettingMapping_OnResponse.class, args);
				
				m_machine.tryPoppingGettingAddressState();
				
				return true;
			}
		}
		
		return false;
	}
	
	private void dispatchGotAddressEvent(Event_Camera_OnAddressResponse.E_Type type, smCellAddress address, smCellAddressMapping mapping)
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
			m_onAddressResponseArgs.init(type, address, mapping);
			m_machine.performAction(Event_Camera_OnAddressResponse.class, m_onAddressResponseArgs);
		}
	}
	
	@Override
	public void onMappingFound(smCellAddress address, smCellAddressMapping mapping)
	{
		if( dispatchGotMappingEvent(Event_GettingMapping_OnResponse.E_Type.ON_FOUND, address, mapping) )
		{
			m_snapToCoordArgs.init(address, mapping.getCoordinate());
			m_machine.performAction(Action_Camera_SnapToCoordinate.class, m_snapToCoordArgs);
		}
	}

	@Override
	public void onMappingNotFound(smCellAddress address)
	{
		dispatchGotMappingEvent(Event_GettingMapping_OnResponse.E_Type.ON_NOT_FOUND, address, null);
	}

	@Override
	public void onResponseError(smCellAddress address)
	{
		dispatchGotMappingEvent(Event_GettingMapping_OnResponse.E_Type.ON_RESPONSE_ERROR, address, null);
	}
	
	@Override
	public void onAddressFound(smCellAddressMapping mapping, smCellAddress address)
	{
		dispatchGotAddressEvent(Event_Camera_OnAddressResponse.E_Type.ON_FOUND, address, mapping);
	}

	@Override
	public void onAddressNotFound(smCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(Event_Camera_OnAddressResponse.E_Type.ON_NOT_FOUND, null, mapping);
	}

	@Override
	public void onResponseError(smCellAddressMapping mapping)
	{
		dispatchGotAddressEvent(Event_Camera_OnAddressResponse.E_Type.ON_RESPONSE_ERROR, null, mapping);
	}
}
