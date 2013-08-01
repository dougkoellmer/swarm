package com.b33hive.server.data.sql;

import com.b33hive.shared.structs.bhTuple;

public interface bhI_SqlEncodable
{
	String getTable();
	
	bhTuple<String, Object> nextColumn();
}