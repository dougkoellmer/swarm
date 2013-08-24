package swarm.shared.json;

import java.util.ArrayList;

import swarm.shared.app.sm;

public class bhJsonQuery
{
	protected static class Condition
	{
		bhI_JsonEncodable m_encodable;
		bhI_JsonKeySource m_keySource;
		Object m_value;
		
		private Condition(bhI_JsonEncodable encodable)
		{
			set(encodable);
		}
		
		private Condition(bhI_JsonKeySource key, Object mustEqual)
		{
			set(key, mustEqual);
		}
		
		protected void set(bhI_JsonEncodable encodable)
		{
			m_encodable = encodable;
			
			m_keySource = null;
			m_value = null;
		}
		
		protected void set(bhI_JsonKeySource key, Object mustEqual)
		{
			m_encodable = null;
			
			m_keySource = key; 
			
			if( mustEqual instanceof Integer)
			{
				mustEqual = ((Integer)mustEqual).doubleValue();
			}
			
			m_value = mustEqual;
		}
		
		private boolean evaluate(bhI_JsonObject json)
		{
			if( m_encodable != null )
			{
				if( m_encodable.isEqualTo(json) )
				{
					return true;
				}
			}
			else
			{
				Object value = sm.jsonFactory.getHelper().getObject(json, m_keySource);
				
				if( value == null && m_value == null )
				{
					return true;
				}
				else if( value != null && m_value != null )
				{
					return value.equals(m_value);
				}
			}
			
			return false;
		}
	}
	
	protected final ArrayList<Condition> m_conditions = new ArrayList<Condition>();
	
	public bhJsonQuery()
	{
		
	}
	
	public void addCondition(bhI_JsonEncodable mustContain)
	{
		m_conditions.add(new Condition(mustContain));
	}
	
	public void addCondition(bhI_JsonKeySource key, Object mustEqual)
	{
		m_conditions.add(new Condition(key, mustEqual));
	}
	
	public boolean evaluate(bhI_JsonObject json)
	{
		for( int i = 0; i < m_conditions.size(); i++ )
		{
			if( !m_conditions.get(i).evaluate(json) )
			{
				return false;
			}
		}
		
		return true;
	}
}
