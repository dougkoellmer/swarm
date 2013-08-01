package com.b33hive.client.ui.tabs.code;

import com.b33hive.shared.debugging.bhU_Debug;
import com.google.gwt.user.client.Element;

public class bhHtmlSandbox
{
	public interface I_StartUpCallback
	{
		void onStartUpComplete(boolean success);
	}
	
	private static bhHtmlSandbox s_instance;
	
	private final bhCajaWrapper m_cajaWrapper;
	
	private boolean m_isRunning = false;
	
	private bhHtmlSandbox(I_StartUpCallback callback)
	{
		m_cajaWrapper = new bhCajaWrapper(callback);
	}
	
	public static bhHtmlSandbox getInstance()
	{
		return s_instance;
	}
	
	public static void startUp(I_StartUpCallback callback)
	{
		s_instance = new bhHtmlSandbox(callback);
	}
	
	public void insertStaticHtml(Element element, String compiledHtml, String idClass)
	{
		m_cajaWrapper.insertStaticHtml(element, compiledHtml, idClass);
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		m_cajaWrapper.allowScrolling(element, yesOrNo);
	}
	
	public void start(Element element, String compiledHtml, String compiledJs, String idClass, final bhI_CodeLoadListener listener)
	{
		if( m_isRunning )
		{
			bhU_Debug.ASSERT(false, "bhHtmlSandbox::start1");
			return;
		}
		
		m_cajaWrapper.start(element, compiledHtml, compiledJs, idClass, listener);
		
		m_isRunning = true;
	}
	
	public boolean isRunning()
	{
		return m_isRunning;
	}
	
	public void stop()
	{
		if( !m_isRunning )
		{
			bhU_Debug.ASSERT(false, "bhHtmlSandbox::stop1");
			return;
		}
		
		m_cajaWrapper.stop();
		
		m_isRunning = false;
	}
}
