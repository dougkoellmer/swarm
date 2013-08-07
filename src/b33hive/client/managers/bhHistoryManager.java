package b33hive.client.managers;

import b33hive.shared.app.bhS_App;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhLinkedListNode;
import b33hive.shared.structs.bhQueue;

public class bhHistoryManager
{
	private static final bhHistoryManager s_instance = new bhHistoryManager();
	
	// TODO: Genericize the queue.
	private final bhQueue m_coords = new bhQueue(S_ClientApp.MAX_HISTORY);
	
	private bhLinkedListNode m_currentNode = null;
	
	private bhHistoryManager()
	{
		
	}
	
	public static bhHistoryManager getInstance()
	{
		return s_instance;
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
