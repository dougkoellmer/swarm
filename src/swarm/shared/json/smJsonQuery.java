package swarm.shared.json;

import java.util.ArrayList;

import swarm.shared.app.smSharedAppContext;

public class smJsonQuery
{
	protected static class Condition
	{
		smI_JsonComparable m_comparable;
		smI_JsonKeySource m_keySource;
		Object m_value;
		
		private Condition(smI_JsonComparable comparable)
		{
			set(m_comparable);
		}
		
		private Condition(smI_JsonKeySource key, Object mustEqual)
		{
			set(key, mustEqual);
		}
		
		protected void set(smI_JsonComparable comparable)
		{
			m_comparable = comparable;
			
			m_keySource = null;
			m_value = null;
		}
		
		protected void set(smI_JsonKeySource key, Object mustEqual)
		{
			m_comparable = null;
			
			m_keySource = key; 
			
			if( mustEqual instanceof Integer)
			{
				mustEqual = ((Integer)mustEqual).doubleValue();
			}
			
			m_value = mustEqual;
		}
		
		private boolean evaluate(smA_JsonFactory factory, smI_JsonObject json)
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
	
	public smJsonQuery()
	{
		
	}
	
	public void addCondition(smI_JsonComparable mustContain)
	{
		m_conditions.add(new Condition(mustContain));
	}
	
	public void addCondition(smI_JsonKeySource key, Object mustEqual)
	{
		m_conditions.add(new Condition(key, mustEqual));
	}
	
	public boolean evaluate(smA_JsonFactory factory, smI_JsonObject json)
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
