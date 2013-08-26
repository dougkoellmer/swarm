package swarm.shared.structs;

public class smQueue extends smLinkedList
{	
	private int m_maxSize = 0;
	
	public smQueue()
	{
		m_maxSize = Integer.MAX_VALUE;
	}
	
	public smQueue(int maxSize)
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
