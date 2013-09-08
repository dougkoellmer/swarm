package swarm.client.view.sandbox;

import swarm.client.view.tabs.code.smI_CodeLoadListener;
import swarm.shared.debugging.smU_Debug;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Element;

public class smInlineFrameSandbox
{
	private final IFrameElement m_iframe;
	
	private boolean m_isRunning;
	
	public smInlineFrameSandbox()
	{
		m_iframe = Document.get().createIFrameElement();
	}
	
	void start_local(Element host, String rawCode, smI_CodeLoadListener listener)
	{
		if( m_iframe.getParentNode() != null )
		{
			smU_Debug.ASSERT(false, "iframe shouldn't have parent.");
			
			m_iframe.removeFromParent();
		}
		
		host.appendChild(m_iframe);
		
		start_local_private(m_iframe, rawCode);
		
		m_isRunning = true;
	}
	
	private native void start_local_private(IFrameElement iframe, String rawCode)
	/*-{
			var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
			iframe.contentWindow.bh = $wnd.bh;
			iframeDoc.write(rawCode);
	}-*/;
	
	void start_remote(Element host, String url, smI_CodeLoadListener listener)
	{
		m_isRunning = true;
	}
	
	void stop()
	{
		if( m_iframe.getParentNode() != null )
		{
			m_iframe.removeFromParent();
		}
		else
		{
			//smU_Debug.ASSERT(false, "iframe should have parent.");
		}
		
		m_isRunning = false;
	}
	
	boolean isRunning()
	{
		return m_isRunning;
	}
}
