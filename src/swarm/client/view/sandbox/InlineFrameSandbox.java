package swarm.client.view.sandbox;

import swarm.client.view.tabs.code.I_CodeLoadListener;
import swarm.shared.debugging.U_Debug;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;

public class InlineFrameSandbox
{
	private final IFrameElement m_iframe;
	private final String m_apiNamespace;
	private final Integer m_version;
	private final String m_versionString;
	
	public InlineFrameSandbox(String apiNamespace, Integer version)
	{
		m_iframe = Document.get().createIFrameElement();
		
		m_iframe.addClassName("sm_iframe_sandbox");
		
		m_apiNamespace = apiNamespace;
		
		m_version = version;
		m_versionString = m_version != null ? "?v="+m_version : "";
	}
	
	private boolean registerLocalApi()
	{
		return registerLocalApi_native(m_iframe, m_apiNamespace);
	}
	
	private native boolean registerLocalApi_native(IFrameElement iframe, String apiNamespace)
	/*-{
			function isSameDomain(iframe)
			{
			    var html = null;
			    try { 
			      // deal with older browsers
			      var doc = iframe.contentDocument || iframe.contentWindow.document;
			      html = doc.body.innerHTML;
			    } catch(err){
			      // do nothing
			    }
			
			    return(html !== null);
			}
		
			if( isSameDomain(iframe) )
			{
				iframe.contentWindow.bh = $wnd.bh;
				iframe.contentWindow.alert = $wnd[apiNamespace+'_alert'];
				
				return true;
			}
			else
			{
				return false;
			}			
	}-*/;
	
	
	private void start_helper(Element host)
	{
		if( m_iframe.getParentNode() != null )
		{
			U_Debug.ASSERT(false, "Expected null parent for iframe.");
			
			m_iframe.removeFromParent();
		}
		
		m_iframe.removeAttribute("src");
		
		host.appendChild(m_iframe);
	}
	
	void start_local(Element host, String rawCode, I_CodeLoadListener listener)
	{
		start_helper(host);
		
		this.start_local_private(m_iframe, rawCode);
		
		listener.onCodeLoad();
	}
	
	private native void start_local_private(IFrameElement iframe, String rawCode)
	/*-{
			var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
			
			this.@swarm.client.view.sandbox.InlineFrameSandbox::registerLocalApi()();
			
			iframeDoc.write(rawCode);
			
			var evt = iframeDoc.createEvent('Event');  
			evt.initEvent('load', false, false);  
			iframe.contentWindow.dispatchEvent(evt);
	}-*/;
	
	void start_remote(Element host, String src, I_CodeLoadListener listener)
	{
		start_helper(host);
		
		start_remote_private(m_iframe, src);
		
		listener.onCodeLoad();
	}
		
	private native void start_remote_private(IFrameElement iframe, String src)
	/*-{
			var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;

			this.@swarm.client.view.sandbox.InlineFrameSandbox::registerLocalApi()();
			var version = this.@swarm.client.view.sandbox.InlineFrameSandbox::m_versionString;
			
			iframe.src = src+version;
	}-*/;
	
	void stop(Element host)
	{
		if( m_iframe.getParentNode() != null )
		{
			if( m_iframe.getParentNode() != host )
			{
				U_Debug.ASSERT(false, "Parent wasn't current host.");
			}
			
			m_iframe.setSrc("data:text/html, ");
			m_iframe.removeFromParent();
		}
	}
}
