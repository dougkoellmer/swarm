package com.b33hive.client.ui.cell;

import java.util.ArrayList;

public class bhAlertManager
{
	private static final int MAX_SIZE = 100;
	public interface I_Delegate
	{
		void showAlert(String message);
	}
	
	private static final bhAlertManager s_instance = new bhAlertManager();
	
	private final ArrayList<String> m_queue = new ArrayList<String>();
	private I_Delegate m_delegate = null;
	
	private bhAlertManager()
	{
		
	}
	
	public static bhAlertManager getInstance()
	{
		return s_instance;
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
