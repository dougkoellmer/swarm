package swarm.client.navigation;

import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.structs.smTolerance;

class smHistoryState extends smA_JsonEncodable
{
	//private static int s_currentFreeIndex = 0;
	
	//private int m_index;
	private smPoint m_point = null;
	private smCellAddressMapping m_mapping = null;
	
	smHistoryState()
	{
		//initIndex();
	}
	
	smHistoryState(smCellAddressMapping mapping)
	{
		m_mapping = mapping;
		
		//initIndex();
	}
	
	smHistoryState(smPoint point)
	{
		m_point = point;
		
		//initIndex();
	}
	
	/*private void initIndex()
	{
		m_index = s_currentFreeIndex;
		
		s_currentFreeIndex++;
	}*/
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		//smU_Json.putInt(json, smE_JsonKey.HISTORY_STATE_INDEX, m_index);
		
		if( m_mapping != null )
		{
			m_mapping.writeJson(factory, json_out);
		}
		
		if( m_point != null )
		{
			m_point.writeJson(factory, json_out);
		}
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		/*m_index = smU_Json.getInt(json, smE_JsonKey.HISTORY_STATE_INDEX);
		
		if( m_index >= s_currentFreeIndex )
		{
			s_currentFreeIndex = m_index + 1;
		}*/
		
		if( smCellAddressMapping.isReadable(factory, json) )
		{
			m_mapping = new smCellAddressMapping();
			m_mapping.readJson(factory, json);
		}
		else if( smPoint.isReadable(factory, json) )
		{
			m_point = new smPoint();
			m_point.readJson(factory, json);
		}
	}
	
	/*public boolean isBefore(smHistoryState state)
	{
		return this.m_index < state.m_index;
	}*/
	
	public boolean isEmpty()
	{
		return m_point == null && m_mapping == null;
	}
	
	public boolean isEqualTo(smHistoryState otherState)
	{
		if( this.m_mapping != null && otherState.m_mapping != null )
		{
			return this.m_mapping.isEqualTo(otherState.m_mapping);
		}
		else if( this.m_point != null && otherState.m_point != null )
		{
			return this.m_point.isEqualTo(otherState.m_point, smTolerance.EXACT);
		}
		else if(	this.m_mapping == null && otherState.m_mapping == null &&
					this.m_point == null && otherState.m_point == null )
		{
			return true;
		}
		
		return false;
	}
	
	public smPoint getPoint()
	{
		return m_point;
	}
	
	public smCellAddressMapping getMapping()
	{
		return m_mapping;
	}
}
