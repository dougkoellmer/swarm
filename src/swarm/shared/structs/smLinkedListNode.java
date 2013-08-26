package swarm.shared.structs;

public class smLinkedListNode
{
	int m_position;
	smLinkedListNode m_next;             // Refers to next item in the list.
	smLinkedListNode m_previous;         // Refers to the previous item.       ***
	Object m_object;               
	
	smLinkedListNode(Object item)
	{
		this.m_object = item;
		m_next = m_previous = null;
	}

	/*public void setNext(smLinkedListNode next)
	{
		this.m_next = next;        // Store reference to the next item.
	}

	// Additional method to set the pointer to the previous smLinkedListNode:  ***
	public void setPrevious(smLinkedListNode previous)
	{                                                               
		this.m_previous = previous; // Store reference to the previous item. 
	}*/

	public smLinkedListNode getNext()
	{
		return m_next;
	}
	
	public smLinkedListNode getPrevious()
	{
		return m_previous;
	}


	public Object getObject()
	{
		return m_object;
	}
}
