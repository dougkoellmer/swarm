package swarm.shared.json;

import java.util.ArrayList;

import swarm.shared.app.BaseAppContext;

public class JsonQuery
{
	protected static class Condition
	{
		I_JsonComparable m_comparable;
		I_JsonKeySource m_keySource;
		Object m_value;
		
		private Condition(I_JsonComparable comparable)
		{
			set(m_comparable);
		}
		
		private Condition(I_JsonKeySource key, Object mustEqual)
		{
			set(key, mustEqual);
		}
		
		protected void set(I_JsonComparable comparable)
		{
			m_comparable = comparable;
			
			m_keySource = null;
			m_value = null;
		}
		
		protected void set(I_JsonKeySource key, Object mustEqual)
		{
			m_comparable = null;
			
			m_keySource = key; 
			
			if( mustEqual instanceof Integer)
			{
				mustEqual = ((Integer)mustEqual).doubleValue();
			}
			
			m_value = mustEqual;
		}
		
		private boolean evaluate(A_JsonFactory factory, I_JsonObject json)
		{
			if( m_comparable != null )
			{
				if( m_comparable.isEqualTo(factory, json) )
				{
					return true;
				}
			}
			else
			{
				Object value = factory.getHelper().getObject(json, m_keySource);
				
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
	
	public JsonQuery()
	{
		
	}
	
	public void addCondition(I_JsonComparable mustContain)
	{
		m_conditions.add(new Condition(mustContain));
	}
	
	public void addCondition(I_JsonKeySource key, Object mustEqual)
	{
		m_conditions.add(new Condition(key, mustEqual));
	}
	
	public boolean evaluate(A_JsonFactory factory, I_JsonObject json)
	{
		for( int i = 0; i < m_conditions.size(); i++ )
		{
			if( !m_conditions.get(i).evaluate(factory, json) )
			{
				return false;
			}
		}
		
		return true;
	}
}
