package swarm.shared.json;

import swarm.shared.json.smJsonQuery.Condition;

public class smMutableJsonQuery extends smJsonQuery
{
	public smMutableJsonQuery()
	{
		
	}
	
	public void setCondition(int index, smI_JsonEncodable mustContain)
	{
		Condition condition = m_conditions.get(index);
		condition.set(mustContain);
	}
	
	public void setCondition(int index, smI_JsonKeySource key, Object mustEqual)
	{
		Condition condition = m_conditions.get(index);
		
		condition.set(key, mustEqual);
	}
}
