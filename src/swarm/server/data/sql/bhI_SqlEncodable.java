package swarm.server.data.sql;

import swarm.shared.structs.bhTuple;

public interface bhI_SqlEncodable
{
	String getTable();
	
	bhTuple<String, Object> nextColumn();
}