package swarm.shared.structs;

public class LinkedList
{
	private LinkedListNode m_first = null;
	private LinkedListNode m_last = null;
	
	public LinkedList()
	{
	}
	
	public int getSize()
	{
		return m_first != null ? (m_last.m_position - m_first.m_position) + 1 : 0;
	}
	
	public void removeFirst()
	{
		m_first = m_first.m_next;
		m_first.m_previous = null;
	}

	public void addObject(Object object)
	{
		LinkedListNode newLast = new LinkedListNode(object);
		if( m_first == null )
		{
			m_first = m_last = newLast;
			newLast.m_position = 0;
		}
		else
		{
			newLast.m_position = m_last.m_position + 1;
			m_last.m_next = newLast;
			newLast.m_previous = m_last;
			m_last = newLast;
		}
	}
	
	public void cropAfter(LinkedListNode node)
	{
		node.m_next = null;
		this.m_last = node;
	}
	
	public LinkedListNode getFirstNode()
	{
		return m_first;
	}
	
	public LinkedListNode getLastNode()
	{
		return m_last;
	}

	public Object getFirst()
	{
		return m_first != null ? m_first.getObject() : null;
	}

	public Object getLast()
	{
		return m_last != null ? m_last.getObject() : null;
	}
}
