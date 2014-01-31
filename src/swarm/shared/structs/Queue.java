package swarm.shared.structs;

public class Queue extends LinkedList
{	
	private int m_maxSize = 0;
	
	public Queue()
	{
		m_maxSize = Integer.MAX_VALUE;
	}
	
	public Queue(int maxSize)
	{
		m_maxSize = maxSize;
	}
	
	public int getMaxSize()
	{
		return m_maxSize;
	}
	
	@Override
	public void addObject(Object object)
	{
		if( this.getSize() == m_maxSize )
		{
			this.removeFirst();
		}
		
		super.addObject(object);
	}
}
