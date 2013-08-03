package b33hive.shared.structs;

public class bhLinkedList
{
	private bhLinkedListNode m_first = null;
	private bhLinkedListNode m_last = null;
	
	public bhLinkedList()
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
		bhLinkedListNode newLast = new bhLinkedListNode(object);
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
	
	public void cropAfter(bhLinkedListNode node)
	{
		node.m_next = null;
		this.m_last = node;
	}
	
	public bhLinkedListNode getFirstNode()
	{
		return m_first;
	}
	
	public bhLinkedListNode getLastNode()
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
