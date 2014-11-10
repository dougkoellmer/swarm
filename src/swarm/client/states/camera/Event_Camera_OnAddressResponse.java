package swarm.client.states.camera;

import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_Action_Event;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;

public class Event_Camera_OnAddressResponse extends A_Action_Event
{
	public static enum E_Type
	{
		ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
	}
	
	public static class Args extends StateArgs
	{
		private CellAddress m_address;
		private CellAddressMapping m_mapping;
		private Event_Camera_OnAddressResponse.E_Type m_responseType;
		
		Args()
		{
			init(null, null, null);
		}
		
		Args(Event_Camera_OnAddressResponse.E_Type responseType, CellAddress address, CellAddressMapping mapping)
		{
			init(responseType, address, mapping);
		}
		
		public void init(Event_Camera_OnAddressResponse.E_Type responseType, CellAddress address, CellAddressMapping mapping)
		{
			m_responseType = responseType;
			m_address = address;
			m_mapping = mapping;
		}
		
		public CellAddress getAddress()
		{
			return m_address;
		}
		
		public CellAddressMapping getMapping()
		{
			return m_mapping;
		}
		
		public Event_Camera_OnAddressResponse.E_Type getType()
		{
			return m_responseType;
		}
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		StateMachine_Camera machine = this.getState();
		A_State currentState = machine.getCurrentState();
		
		return currentState instanceof State_CameraSnapping || currentState instanceof State_ViewingCell;
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}