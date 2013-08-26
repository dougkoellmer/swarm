package swarm.shared.structs;

public class smTuple <T, U>
{
	private final T m_first;
	private final U m_second;
	
	public smTuple(T first, U second)
	{
		m_first = first;
		m_second = second;
	}
	
	public T getFirst()
	{
		return m_first;
	}
	
	public U getSecond()
	{
		return m_second;
	}
}
