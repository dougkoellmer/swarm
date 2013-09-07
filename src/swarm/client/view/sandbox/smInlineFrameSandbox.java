package swarm.client.view.sandbox;

import swarm.client.view.tabs.code.smI_CodeLoadListener;

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
		m_isRunning = true;
	}
	
	void start_remote(Element host, String url, smI_CodeLoadListener listener)
	{
		m_isRunning = true;
	}
	
	void stop()
	{
		m_isRunning = false;
	}
	
	boolean isRunning()
	{
		return m_isRunning;
	}
}
