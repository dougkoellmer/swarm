package swarm.shared.utils;

import java.util.ArrayList;

public class bhListenerManager<T_extends_interface>
{
	private final ArrayList<T_extends_interface> m_listeners = new ArrayList<T_extends_interface>();
	
	public void addListenerToFront(T_extends_interface listener)
	{
		m_listeners.add(0, listener);
	}
	
	public void addListenerToBack(T_extends_interface listener)
	{
		m_listeners.add(listener);
	}
	
	public void removeListener(T_extends_interface listener)
	{
		for( int i = m_listeners.size()-1; i >= 0; i-- )
		{
			if( listener == m_listeners.get(i) )
			{
				m_listeners.remove(i);
			}
		}
	}
	
	/*
	 * Little sloppy to return a mutable array, but returning iterator still makes it mutable.
	 */
	public ArrayList<T_extends_interface> getListeners()
	{
		return m_listeners;
	}
}