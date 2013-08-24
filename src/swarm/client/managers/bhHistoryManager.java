package swarm.client.managers;

import swarm.shared.app.bhS_App;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhLinkedListNode;
import swarm.shared.structs.bhQueue;

public class bhHistoryManager
{	
	// TODO: Genericize the queue.
	private final bhQueue m_coords;
	
	private bhLinkedListNode m_currentNode = null;
	
	private bhHistoryManager(int maxHistory)
	{
		m_coords = new bhQueue(maxHistory);
	}
	
	public void pushCoordinate(int m, int n)
	{
		if( m_currentNode != null )
		{
			bhGridCoordinate currentCoord = (bhGridCoordinate) m_currentNode.getObject();
			if( currentCoord.isEqualTo(m, n) )
			{
				return;
			}
			
			m_coords.cropAfter(m_currentNode);
		}
		
		m_coords.addObject(new bhGridCoordinate(m, n));
		
		m_currentNode = m_coords.getLastNode();
	}
	
	public boolean hasNext()
	{
		return m_currentNode != null && m_currentNode.getNext() != null;
	}
	
	public bhGridCoordinate getNext()
	{
		if( !hasNext() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getNext();
		
		bhGridCoordinate coord = (bhGridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
	
	public boolean hasPrevious()
	{
		return m_currentNode != null && m_currentNode.getPrevious() != null;
	}
	
	public bhGridCoordinate getPrevious()
	{
		if( !hasPrevious() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getPrevious();

		bhGridCoordinate coord = (bhGridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
}
