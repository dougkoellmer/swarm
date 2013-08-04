package b33hive.client.ui.tabs.code;

import b33hive.shared.debugging.bhU_Debug;
import com.google.gwt.user.client.Element;

public class bhHtmlSandbox
{
	public interface I_StartUpCallback
	{
		void onStartUpComplete(boolean success);
	}
	
	private final bhCajaWrapper m_cajaWrapper;
	
	private boolean m_isRunning = false;
	
	public bhHtmlSandbox(I_StartUpCallback callback, String apiNamespace)
	{
		m_cajaWrapper = new bhCajaWrapper(callback, apiNamespace);
	}
	
	public void insertStaticHtml(Element element, String compiledHtml, String cellNamespace)
	{
		m_cajaWrapper.insertStaticHtml(element, compiledHtml, cellNamespace);
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		m_cajaWrapper.allowScrolling(element, yesOrNo);
	}
	
	public void start(Element element, String compiledHtml, String compiledJs, String cellNamespace, final bhI_CodeLoadListener listener)
	{
		if( m_isRunning )
		{
			bhU_Debug.ASSERT(false, "bhHtmlSandbox::start1");
			return;
		}
		
		m_cajaWrapper.start(element, compiledHtml, compiledJs, cellNamespace, listener);
		
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
