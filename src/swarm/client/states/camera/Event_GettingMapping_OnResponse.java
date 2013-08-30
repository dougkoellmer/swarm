package swarm.client.states.camera;

import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_EventAction;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;

public class Event_GettingMapping_OnResponse extends smA_EventAction
{
	public static enum E_Type
	{
		ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
	}
	
	public static class Args extends smA_ActionArgs
	{
		private final smCellAddress m_address;
		private final smCellAddressMapping m_mapping;
		private final Event_GettingMapping_OnResponse.E_Type m_responseType;
		
		Args(Event_GettingMapping_OnResponse.E_Type responseType, smCellAddress address, smCellAddressMapping mapping )
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
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return State_GettingMapping.class;
	}
}