package b33hive.shared.structs;

public class bhLinkedListNode
{
	int m_position;
	bhLinkedListNode m_next;             // Refers to next item in the list.
	bhLinkedListNode m_previous;         // Refers to the previous item.       ***
	Object m_object;               
	
	bhLinkedListNode(Object item)
	{
		this.m_object = item;
		m_next = m_previous = null;
	}

	/*public void setNext(bhLinkedListNode next)
	{
		this.m_next = next;        // Store reference to the next item.
	}

	// Additional method to set the pointer to the previous bhLinkedListNode:  ***
	public void setPrevious(bhLinkedListNode previous)
	{                                                               
		this.m_previous = previous; // Store reference to the previous item. 
	}*/

	public bhLinkedListNode getNext()
	{
		return m_next;
	}
	
	public bhLinkedListNode getPrevious()
	{
		return m_previous;
	}


	public Object getObject()
	{
		return m_object;
	}
}
