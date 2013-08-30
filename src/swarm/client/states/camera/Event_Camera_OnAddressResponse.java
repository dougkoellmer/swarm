package swarm.client.states.camera;

import swarm.client.states.camera.StateMachine_Camera.Event_OnAddressResponse.E_Type;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_EventAction;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;

public class Event_Camera_OnAddressResponse extends smA_EventAction
{
	public static enum E_Type
	{
		ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
	}
	
	public static class Args extends smA_ActionArgs
	{
		private final smCellAddress m_address;
		private final smCellAddressMapping m_mapping;
		private final Event_Camera_OnAddressResponse.E_Type m_responseType;
		
		Args(Event_Camera_OnAddressResponse.E_Type responseType, smCellAddress address, smCellAddressMapping mapping )
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
		
		public Event_Camera_OnAddressResponse.E_Type getType()
		{
			return m_responseType;
		}
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		smA_State currentState = machine.getCurrentState();
		
		return currentState instanceof State_CameraSnapping || currentState instanceof State_ViewingCell;
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return StateMachine_Camera.class;
	}
}