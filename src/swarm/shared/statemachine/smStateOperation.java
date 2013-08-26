package swarm.shared.statemachine;

public class smStateOperation
{
	private final smE_OperationType m_type;
	private final smA_StateConstructor m_constructor;
	
	public smStateOperation(smE_OperationType type, smA_StateConstructor constructor)
	{
		m_type = type;
		m_constructor = constructor;
	}
	
	public smE_OperationType getType()
	{
		return m_type;
	}
	
	public smA_StateConstructor getConstructor()
	{
		return m_constructor;
	}
}
