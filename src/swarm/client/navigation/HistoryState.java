package swarm.client.navigation;

import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.statemachine.A_Action;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.structs.Tolerance;

class HistoryState extends A_JsonEncodable
{
	private static final String ID_KEY = "sm_state_id";
	private static final String HIGHEST_ID_KEY = "sm_state_highest_id";
	
	//private static int s_currentFreeIndex = 0;
	
	//private int m_index;
	private Point m_point = null;
	private CellAddressMapping m_mapping = null;
	private int m_id;
	private int m_highestId = 0;
	
	HistoryState()
	{
		//initIndex();
	}
	
	HistoryState(CellAddressMapping mapping)
	{
		m_mapping = mapping;
		
		//initIndex();
	}
	
	HistoryState(Point point)
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
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
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
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		/*m_index = smU_Json.getInt(json, smE_JsonKey.HISTORY_STATE_INDEX);
		
		if( m_index >= s_currentFreeIndex )
		{
			s_currentFreeIndex = m_index + 1;
		}*/
		
		if( CellAddressMapping.isReadable(factory, json) )
		{
			m_mapping = new CellAddressMapping();
			m_mapping.readJson(factory, json);
		}
		else if( Point.isReadable(factory, json) )
		{
			m_point = new Point();
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
	
	public boolean isEqualTo(HistoryState otherState)
	{
		if( this.m_mapping != null && otherState.m_mapping != null )
		{
			return this.m_mapping.isEqualTo(otherState.m_mapping);
		}
		else if( this.m_point != null && otherState.m_point != null )
		{
			return this.m_point.isEqualTo(otherState.m_point, Tolerance.EXACT);
		}
		else if(	this.m_mapping == null && otherState.m_mapping == null &&
					this.m_point == null && otherState.m_point == null )
		{
			return true;
		}
		
		return false;
	}
	
	public Point getPoint()
	{
		return m_point;
	}
	
	public CellAddressMapping getMapping()
	{
		return m_mapping;
	}
}
