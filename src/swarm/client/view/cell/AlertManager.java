package swarm.client.view.cell;

import java.util.ArrayList;

public class AlertManager
{
	private static final int MAX_SIZE = 100;
	public interface I_Delegate
	{
		void showAlert(String message);
	}
	
	private final ArrayList<String> m_queue = new ArrayList<String>();
	private I_Delegate m_delegate = null;
	
	public AlertManager()
	{
		
	}
	
	public void setDelegate(I_Delegate delegate)
	{
		m_delegate = delegate;
	}
	
	private void showAlertIfNeeded()
	{
		if( m_queue.size() >= 1 )
		{
			String message = m_queue.get(0);
			
			if( m_delegate != null )
			{
				m_delegate.showAlert(message);
			}
		}
	}
	
	public void onHandled()
	{
		m_queue.remove(0);
		
		showAlertIfNeeded();
	}
	
	public void queue(String message)
	{
		if( m_queue.size() >= MAX_SIZE )
		{
			return;
		}
		
		m_queue.add(message);
		
		if( m_queue.size() == 1 )
		{
			showAlertIfNeeded();
		}
	}
	
	public void clear()
	{
		m_queue.clear();
	}
}
