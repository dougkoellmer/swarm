package com.b33hive.shared.json;

import com.b33hive.shared.json.bhJsonQuery.Condition;

public class bhMutableJsonQuery extends bhJsonQuery
{
	public bhMutableJsonQuery()
	{
		
	}
	
	public void setCondition(int index, bhI_JsonEncodable mustContain)
	{
		Condition condition = m_conditions.get(index);
		condition.set(mustContain);
	}
	
	public void setCondition(int index, bhI_JsonKeySource key, Object mustEqual)
	{
		Condition condition = m_conditions.get(index);
		
		condition.set(key, mustEqual);
	}
}
