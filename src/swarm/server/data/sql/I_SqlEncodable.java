package swarm.server.data.sql;

import swarm.shared.structs.Tuple;

public interface I_SqlEncodable
{
	String getTable();
	
	Tuple<String, Object> nextColumn();
}