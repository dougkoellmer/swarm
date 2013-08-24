package swarm.shared.structs;

public class bhQueue extends bhLinkedList
{	
	private int m_maxSize = 0;
	
	public bhQueue()
	{
		m_maxSize = Integer.MAX_VALUE;
	}
	
	public bhQueue(int maxSize)
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
