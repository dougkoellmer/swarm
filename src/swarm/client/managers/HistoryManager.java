package swarm.client.managers;

import swarm.shared.app.S_CommonApp;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.LinkedListNode;
import swarm.shared.structs.Queue;

public class HistoryManager
{	
	// TODO: Genericize the queue.
	private final Queue m_coords;
	
	private LinkedListNode m_currentNode = null;
	
	private HistoryManager(int maxHistory)
	{
		m_coords = new Queue(maxHistory);
	}
	
	public void pushCoordinate(int m, int n)
	{
		if( m_currentNode != null )
		{
			GridCoordinate currentCoord = (GridCoordinate) m_currentNode.getObject();
			if( currentCoord.isEqualTo(m, n) )
			{
				return;
			}
			
			m_coords.cropAfter(m_currentNode);
		}
		
		m_coords.addObject(new GridCoordinate(m, n));
		
		m_currentNode = m_coords.getLastNode();
	}
	
	public boolean hasNext()
	{
		return m_currentNode != null && m_currentNode.getNext() != null;
	}
	
	public GridCoordinate getNext()
	{
		if( !hasNext() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getNext();
		
		GridCoordinate coord = (GridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
	
	public boolean hasPrevious()
	{
		return m_currentNode != null && m_currentNode.getPrevious() != null;
	}
	
	public GridCoordinate getPrevious()
	{
		if( !hasPrevious() )
		{
			return null;
		}

		m_currentNode = m_currentNode.getPrevious();

		GridCoordinate coord = (GridCoordinate) m_currentNode.getObject();
		
		return coord;
	}
}
