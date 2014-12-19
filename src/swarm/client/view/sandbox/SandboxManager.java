package swarm.client.view.sandbox;

import java.util.logging.Logger;

import swarm.client.view.ViewContext;
import swarm.client.view.tabs.code.I_CodeLoadListener;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.structs.Code;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;

public class SandboxManager
{
	private static final Logger s_logger = Logger.getLogger(SandboxManager.class.getName());
	
	public interface I_StartUpCallback
	{
		void onStartUpComplete(boolean success);
	}
	
	private final CajaSandboxManager m_cajaSandboxMngr;
	private final InlineFrameSandboxManager m_iframeSandboxMngr;
	
	private final CellApi m_cellApi;
	
	public SandboxManager(ViewContext viewContext, I_StartUpCallback callback, String apiNamespace, boolean useVirtualSandbox)
	{
		m_cellApi = new CellApi(viewContext);
		m_cellApi.registerApi(apiNamespace);
		
		Integer version = viewContext.appConfig.appVersion + viewContext.appConfig.libVersion;
		m_iframeSandboxMngr = new InlineFrameSandboxManager(apiNamespace, version);
		m_cajaSandboxMngr = new CajaSandboxManager(m_cellApi, callback, apiNamespace, useVirtualSandbox);
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		if( m_cajaSandboxMngr != null )
		{
			m_cajaSandboxMngr.allowScrolling(element, yesOrNo);
		}
	}
	
	private static final native Node removeAllChildren(Element element)
	/*-{
	    while (element.lastChild) {
	      element.removeChild(element.lastChild);
	    }
	}-*/;
	
	public void start(Element host, Code code, String cellNamespace, I_CodeLoadListener listener)
	{
		E_CodeSafetyLevel codeLevel = code.getSafetyLevel();
		//codeLevel = codeLevel == smE_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX ? smE_CodeSafetyLevel.LOCAL_SANDBOX : codeLevel;
		
		switch(codeLevel)
		{
			case NO_SANDBOX_STATIC:
			case NO_SANDBOX_DYNAMIC:
			case META_IMAGE:
			{
				if( m_cajaSandboxMngr != null )
				{
					m_cajaSandboxMngr.stop(host);
				}
				
				m_iframeSandboxMngr.stop(host);
				
				if( codeLevel == E_CodeSafetyLevel.META_IMAGE )
				{
					removeAllChildren(host);
				}
				else
				{
					host.setInnerHTML(code.getRawCode());
				}
				
				listener.onCodeLoad();
				
				break;
			}
			case LOCAL_SANDBOX:
			{
				if( m_cajaSandboxMngr != null )
				{
					m_cajaSandboxMngr.stop(host);
				}
				
				host.setInnerHTML("");
				
				m_iframeSandboxMngr.start_local(host, code.getRawCode(), listener);  break;
			}
			case REMOTE_SANDBOX:
			{
				if( m_cajaSandboxMngr != null )
				{
					m_cajaSandboxMngr.stop(host);
				}
				
				host.setInnerHTML("");
				
				m_iframeSandboxMngr.start_remote(host, code.getRawCode(), listener);
				
				break;
			}
			case VIRTUAL_DYNAMIC_SANDBOX:
			{
				m_iframeSandboxMngr.stop(host);
				
				if( m_cajaSandboxMngr != null )
				{
					m_cajaSandboxMngr.start_dynamic(host, code.getRawCode(), cellNamespace, listener);
				}
				
				break;
			}
			
			case VIRTUAL_STATIC_SANDBOX:
			{
				m_iframeSandboxMngr.stop(host);
				
				if( m_cajaSandboxMngr != null )
				{
					m_cajaSandboxMngr.start_static(host, code.getRawCode(), cellNamespace, listener);
				}
					
				break;
			}
		}
	}
	
	/*public boolean isRunning()
	{
		return m_cajaSandboxMngr.isRunning() || m_iframeSandboxMngr.isRunning();
	}*/
	
	public void stop(Element host)
	{
		if( m_cajaSandboxMngr != null )
		{
			m_cajaSandboxMngr.stop(host);
		}
		
		m_iframeSandboxMngr.stop(host);
		
		removeAllChildren(host);
	}
}

/*
 * Uncaught TypeError: Cannot read property 'e' of null 39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html:1565
com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at Unknown.E3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.K3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@68)
  at Unknown.dr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@1589)
  at Unknown.Ur(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@187)
  at Unknown.Mr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@77)
  at Unknown.vh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@30)
  at Unknown.jp(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@115)
  at Unknown.xh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@120)
  at Unknown.Gh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@13652)
  at Unknown.tL(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@80)
Caused by: com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at Unknown.E3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.K3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@68)
  at Unknown.dr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@1589)
  at Unknown.Ur(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@187)
  at Unknown.Mr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@77)
  at Unknown.vh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@30)
  at Unknown.jp(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@115)
  at Unknown.xh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@120)
  at Unknown.Gh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@13652)
  at Unknown.tL(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@80) 39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html:2207
20com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at Unknown.E3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.K3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@68)
  at Unknown.dr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@1589)
  at Unknown.Ur(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@187)
  at Unknown.Mr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@77)
  at Unknown.vh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@30)
  at Unknown.jp(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@115)
  at Unknown.xh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@120)
  at Unknown.Gh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@13652)
  at Unknown.tL(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@80)
  at Unknown.<anonymous>(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@160)
  at Unknown.Ji(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.Mi(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@57)
  at Unknown.<anonymous>(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@45)
Caused by: com.google.gwt.core.client.JavaScriptException: (TypeError) : Cannot read property 'g' of null
  at Unknown.E3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.K3(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@68)
  at Unknown.dr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@1589)
  at Unknown.Ur(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@187)
  at Unknown.Mr(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@77)
  at Unknown.vh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@30)
  at Unknown.jp(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@115)
  at Unknown.xh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@120)
  at Unknown.Gh(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@13652)
  at Unknown.tL(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@80)
  at Unknown.<anonymous>(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@160)
  at Unknown.Ji(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@29)
  at Unknown.Mi(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@57)
  at Unknown.<anonymous>(http://127.0.0.1:8888/r.app/39A6DEC9717AB51F7E6AB3A63A4F7A0C.cache.html@45) 
  */
