package swarm.client.navigation;

import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;
import swarm.shared.structs.bhTolerance;

class bhHistoryState extends bhA_JsonEncodable
{
	//private static int s_currentFreeIndex = 0;
	
	//private int m_index;
	private bhPoint m_point = null;
	private bhCellAddressMapping m_mapping = null;
	
	bhHistoryState()
	{
		//initIndex();
	}
	
	bhHistoryState(bhCellAddressMapping mapping)
	{
		m_mapping = mapping;
		
		//initIndex();
	}
	
	bhHistoryState(bhPoint point)
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
	public void writeJson(bhI_JsonObject json)
	{
		//bhU_Json.putInt(json, bhE_JsonKey.HISTORY_STATE_INDEX, m_index);
		
		if( m_mapping != null )
		{
			m_mapping.writeJson(json);
		}
		
		if( m_point != null )
		{
			m_point.writeJson(json);
		}
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		/*m_index = bhU_Json.getInt(json, bhE_JsonKey.HISTORY_STATE_INDEX);
		
		if( m_index >= s_currentFreeIndex )
		{
			s_currentFreeIndex = m_index + 1;
		}*/
		
		if( bhCellAddressMapping.isReadable(json) )
		{
			m_mapping = new bhCellAddressMapping();
			m_mapping.readJson(json);
		}
		else if( bhPoint.isReadable(json) )
		{
			m_point = new bhPoint();
			m_point.readJson(json);
		}
	}
	
	/*public boolean isBefore(bhHistoryState state)
	{
		return this.m_index < state.m_index;
	}*/
	
	public boolean isEmpty()
	{
		return m_point == null && m_mapping == null;
	}
	
	public boolean isEqualTo(bhHistoryState otherState)
	{
		if( this.m_mapping != null && otherState.m_mapping != null )
		{
			return this.m_mapping.isEqualTo(otherState.m_mapping);
		}
		else if( this.m_point != null && otherState.m_point != null )
		{
			return this.m_point.isEqualTo(otherState.m_point, bhTolerance.EXACT);
		}
		else if(	this.m_mapping == null && otherState.m_mapping == null &&
					this.m_point == null && otherState.m_point == null )
		{
			return true;
		}
		
		return false;
	}
	
	public bhPoint getPoint()
	{
		return m_point;
	}
	
	public bhCellAddressMapping getMapping()
	{
		return m_mapping;
	}
}
