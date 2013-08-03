package b33hive.shared.statemachine;

import java.util.ArrayList;
import java.util.logging.Logger;

import b33hive.client.states.code.State_EditingCodeBlocker;
import b33hive.shared.debugging.bhU_Debug;

public class bhStateTreeRoot
{
	private static final Logger s_logger = Logger.getLogger(bhStateTreeRoot.class.getName());
	
	private final bhA_State m_rootState;
	
	private final ArrayList<bhI_StateEventListener> m_listeners = new ArrayList<bhI_StateEventListener>();
	private final ArrayList<bhStateEvent> m_eventQueue = new ArrayList<bhStateEvent>();
	private int m_eventQueueIndex = 0;
	
	private boolean m_processEventQueue_hasEntered = false;
	
	private int m_queueEvent_recursionDepth = 0;
	
	bhStateTreeRoot(bhA_State rootState)
	{
		m_rootState = rootState;
	}
	
	public bhA_State getRootState()
	{
		return m_rootState;
	}
	
	void beginBatch()
	{
		m_queueEvent_recursionDepth++;
	}
	
	void endBatch()
	{
		processEventQueue();
	}
	
	void queueEvent(bhStateEvent newEvent)
	{
		boolean findAntiEvents = false;

		switch( newEvent.getType() )
		{
			case DID_EXIT:
			case DID_BACKGROUND:
			{
				findAntiEvents = true;
			}
		}
		
		boolean antiMatterExplosion = false;
		
		if( findAntiEvents )
		{
			for( int i = m_eventQueue.size()-1; i >= m_eventQueueIndex; i-- )
			{
				bhStateEvent pastEvent = m_eventQueue.get(i);
				
				if( pastEvent == null )
				{
					continue;
				}
				
				if( pastEvent.m_state == newEvent.getState() )
				{
					if( newEvent.getType() == bhE_StateEventType.DID_EXIT )
					{
						if( pastEvent.getType() == bhE_StateEventType.DID_ENTER )
						{
							antiMatterExplosion = true;
						}
						/*else if( pastEvent.getType() == bhE_StateEventType.DID_PERFORM_ACTION )
						{
							antiMatterExplosion = true;
						}*/
					}
					else if( newEvent.getType() == bhE_StateEventType.DID_BACKGROUND )
					{
						if( pastEvent.getType() == bhE_StateEventType.DID_FOREGROUND )
						{
							antiMatterExplosion = true;
						}
						/*else if( pastEvent.getType() == bhE_StateEventType.DID_PERFORM_ACTION )
						{
							if( !pastEvent.m_action.isPerformableInBackground() )
							{
								antiMatterExplosion = true;
							}
						}*/
					}
				}
				
				if( antiMatterExplosion )
				{
					m_eventQueue.set(i, null);
					
					break;
				}
			}
		}
		
		if( !antiMatterExplosion )
		{
			m_eventQueue.add(newEvent);
		}
		
		m_queueEvent_recursionDepth++;
	}
	
	void addListener(bhI_StateEventListener listener)
	{
		m_listeners.add(listener);
	}
	
	void removeListener(bhI_StateEventListener listener)
	{
		for( int i = m_listeners.size()-1; i >= 0; i-- )
		{
			if( m_listeners.get(i) == listener )
			{
				if( !m_processEventQueue_hasEntered )
				{
					m_listeners.remove(i);
				}
				else
				{
					m_listeners.set(i, null);
				}
				
				break;
			}
		}
	}
	
	private void cleanListeners()
	{
		bhU_Debug.ASSERT(!m_processEventQueue_hasEntered);
		
		for( int i = m_listeners.size()-1; i >= 0; i-- )
		{
			if( m_listeners.get(i) == null )
			{
				m_listeners.remove(i);
				
				break;
			}
		}
	}
	
	void processEventQueue()
	{
		m_queueEvent_recursionDepth--;
		
		if( m_queueEvent_recursionDepth > 0 )
		{
			return;
		}
		
		boolean firstEntryIntoMethod = false;
		
		if( !m_processEventQueue_hasEntered )
		{
			m_eventQueueIndex = 0;
			
			firstEntryIntoMethod = true;

			m_processEventQueue_hasEntered = true;
		}
		
		if( !firstEntryIntoMethod )
		{
			return;
		}
		
		if( m_listeners.size() == 0 )
		{
			m_eventQueue.clear();
			m_eventQueueIndex = 0;
			
			m_processEventQueue_hasEntered = false;
			
			return;
		}
		
		//--- DRK > NOTE: Keeping the indices as members of this class is useless now,
		//---				but at some point was necessary as recursive calls to this method were possible.
		//---				Just keeping them as members for now because this class is a house of cards.
		while( m_eventQueueIndex < m_eventQueue.size() )
		{
			bhStateEvent event = m_eventQueue.get(m_eventQueueIndex);
			
			if( event == null )
			{
				m_eventQueueIndex++;
				
				continue;
			}

			if( event.m_listenerIndex == bhStateEvent.UNINITIALIZED_LISTENER_INDEX )
			{
				event.m_listenerIndex = m_listeners.size()-1;
			}
			
			while( event.m_listenerIndex >= 0 )
			{
				int listenerIndex = event.m_listenerIndex;
				event.m_listenerIndex--;
				
				if( event.m_listenerIndex == -1 )
				{
					m_eventQueueIndex++;
				}
				
				//--- DRK > Listener at this index can be null if it was removed while m_processEventQueue_hasEntered == true.
				bhI_StateEventListener listener = m_listeners.get(listenerIndex);
				if( listener != null )
				{
					event.dispatch(listener);
				}

				//--- DRK > If we still have listeners to go, and it appears that this event was cancelled out inside
				//---		the last dispatch, we continue our merry way along the event queue.
				if( event.m_listenerIndex >= 0 )
				{
					if( m_eventQueue.get(m_eventQueueIndex) == null )
					{
						break;
					}
				}
			}
		}
		
		if( m_eventQueueIndex >= m_eventQueue.size() )
		{
			bhU_Debug.ASSERT(m_eventQueueIndex == m_eventQueue.size() );
			
			m_eventQueue.clear();
			m_eventQueueIndex= 0;
		}
		
		if( firstEntryIntoMethod )
		{
			m_processEventQueue_hasEntered = false;
			
			cleanListeners();
		}
	}
}
