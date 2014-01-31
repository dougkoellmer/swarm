package swarm.shared.json;

import swarm.shared.json.JsonQuery.Condition;

public class MutableJsonQuery extends JsonQuery
{
	public MutableJsonQuery()
	{
		
	}
	
	public void setCondition(int index, I_JsonComparable mustContain)
	{
		Condition condition = m_conditions.get(index);
		condition.set(mustContain);
	}
	
	public void setCondition(int index, I_JsonKeySource key, Object mustEqual)
	{
		Condition condition = m_conditions.get(index);
		
		condition.set(key, mustEqual);
	}
}
