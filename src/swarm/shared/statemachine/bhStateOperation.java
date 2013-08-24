package swarm.shared.statemachine;

public class bhStateOperation
{
	private final bhE_OperationType m_type;
	private final bhA_StateConstructor m_constructor;
	
	public bhStateOperation(bhE_OperationType type, bhA_StateConstructor constructor)
	{
		m_type = type;
		m_constructor = constructor;
	}
	
	public bhE_OperationType getType()
	{
		return m_type;
	}
	
	public bhA_StateConstructor getConstructor()
	{
		return m_constructor;
	}
}
