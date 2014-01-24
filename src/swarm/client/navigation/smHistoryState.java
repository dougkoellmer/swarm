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
	private static final String ID_KEY = "sm_state_id";
	private static final String HIGHEST_ID_KEY = "sm_state_highest_id";
	
	//private static int s_currentFreeIndex = 0;
	
	//private int m_index;
	private smPoint m_point = null;
	private smCellAddressMapping m_mapping = null;
	private int m_id;
	private int m_highestId = 0;
	
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
	
	void setIds(int id, int highestId)
	{
		m_id = id;
		m_highestId = highestId;
	}
	
	int getId()
	{
		return m_id;
	}
	
	int getHighestId()
	{
		return m_highestId;
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
		
		json_out.putInt(ID_KEY, m_id);
		json_out.putInt(HIGHEST_ID_KEY, m_highestId);
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
		
		m_id = json.containsKey(ID_KEY) ? json.getInt(ID_KEY) : 0;
		m_highestId = json.containsKey(HIGHEST_ID_KEY) ? json.getInt(HIGHEST_ID_KEY) : 0;
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
