package swarm.server.data.sql;

import swarm.shared.structs.smTuple;

public interface smI_SqlEncodable
{
	String getTable();
	
	smTuple<String, Object> nextColumn();
}