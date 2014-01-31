package swarm.shared.transaction;

public interface I_RequestPath
{	
	int getId();

	String getName();
	
	E_HttpMethod getDefaultMethod();
}
