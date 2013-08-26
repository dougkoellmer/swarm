package swarm.client.managers;

import swarm.shared.app.smS_App;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smLinkedListNode;
import swarm.shared.structs.smQueue;

public class smHistoryManager
{	
	// TODO: Genericize the queue.
	private final smQueue m_coords;
	
	private bhLinkedListNode m_currentNode = null;
	
	private bhHistoryManager(int maxHistory)
	{
		m_coords = new smQueue(maxHistory);
	}
	
	public void pushCoordinate(int m, int n)
	{
		if( m_currentNode != null )
		{
			bhGridCoordinate currentCoord = (smGridCoordinate) m_currentNode.getObject();
			if( currentCoord.isEqualTo(m, n) )
			{
				return;
			}
			
			m_coords.cropAfter(m_currentNode);
		}
		
		m_coords.addObject(new smGridCoordinate(m, n));
		
		m_currentNode = m_coords.getLastNode();
	}
	
	public boolean hasNext()
	{
		return m_currentNode != null && m_currentNode.getNext() != null;
	}
	
	public smGridCoordinate getNext()
	{
		if( !hasNext() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getNext();
		
		bhGridCoordinate coord = (smGridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
	
	public boolean hasPrevious()
	{
		return m_currentNode != null && m_currentNode.getPrevious() != null;
	}
	
	public smGridCoordinate getPrevious()
	{
		if( !hasPrevious() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getPrevious();

		bhGridCoordinate coord = (smGridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
}
