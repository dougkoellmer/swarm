package swarm.client.states.camera;

import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_EventAction;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;

public class Event_GettingMapping_OnResponse extends A_EventAction
{
	public static enum E_Type
	{
		ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
	}
	
	public static class Args extends A_ActionArgs
	{
		private final CellAddress m_address;
		private final CellAddressMapping m_mapping;
		private final Event_GettingMapping_OnResponse.E_Type m_responseType;
		
		Args(Event_GettingMapping_OnResponse.E_Type responseType, CellAddress address, CellAddressMapping mapping )
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
		
		public Event_GettingMapping_OnResponse.E_Type getType()
		{
			return m_responseType;
		}
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}