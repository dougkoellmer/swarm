package swarm.client.view.tabs.code;

import swarm.shared.debugging.smU_Debug;
import com.google.gwt.user.client.Element;

public class smCellSandbox
{
	public interface I_StartUpCallback
	{
		void onStartUpComplete(boolean success);
	}
	
	private final smCajaWrapper m_cajaWrapper;
	
	private boolean m_isRunning = false;
	
	public smCellSandbox(I_StartUpCallback callback, String apiNamespace)
	{
		m_cajaWrapper = new smCajaWrapper(callback, apiNamespace);
	}
	
	public void insertStaticHtml(Element element, String compiledHtml, String cellNamespace)
	{
		m_cajaWrapper.insertStaticHtml(element, compiledHtml, cellNamespace);
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		m_cajaWrapper.allowScrolling(element, yesOrNo);
	}
	
	public void start(Element element, String compiledHtml, String compiledJs, String cellNamespace, final smI_CodeLoadListener listener)
	{
		if( m_isRunning )
		{
			smU_Debug.ASSERT(false, "smHtmlSandbox::start1");
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
			smU_Debug.ASSERT(false, "smHtmlSandbox::stop1");
			return;
		}
		
		m_cajaWrapper.stop();
		
		m_isRunning = false;
	}
}
