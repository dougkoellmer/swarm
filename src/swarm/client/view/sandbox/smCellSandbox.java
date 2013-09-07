package swarm.client.view.sandbox;

import swarm.client.view.smViewContext;
import swarm.client.view.tabs.code.smI_CodeLoadListener;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.structs.smCode;

import com.google.gwt.user.client.Element;

public class smCellSandbox
{
	public interface I_StartUpCallback
	{
		void onStartUpComplete(boolean success);
	}
	
	private final smCajaWrapper m_cajaWrapper;
	private final smInlineFrameSandbox m_iframeSandbox;
	
	private final smCellApi m_cellApi;
	
	public smCellSandbox(smViewContext viewContext, I_StartUpCallback callback, String apiNamespace)
	{
		m_cellApi = new smCellApi(viewContext);
		m_cellApi.registerApi(apiNamespace);
		
		m_cajaWrapper = new smCajaWrapper(m_cellApi, callback, apiNamespace);
		m_iframeSandbox = new smInlineFrameSandbox();
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		m_cajaWrapper.allowScrolling(element, yesOrNo);
	}
	
	public void start(Element host, smCode code, String cellNamespace, smI_CodeLoadListener listener)
	{
		if( isRunning() )
		{
			smU_Debug.ASSERT(false, "smHtmlSandbox::start1");
			
			return;
		}
		
		switch(code.getSafetyLevel())
		{
			case NO_SANDBOX:
			{
				host.setInnerHTML(code.getRawCode());  break;
			}
			case LOCAL_SANDBOX:
			{
				m_iframeSandbox.start_local(host, code.getRawCode(), listener);  break;
			}
			case REMOTE_SANDBOX:
			{
				m_iframeSandbox.start_remote(host, code.getRawCode(), listener);  break;
			}
			case VIRTUAL_DYNAMIC_SANDBOX:
			{
				m_cajaWrapper.start_virtualDynamic(host, code.getRawCode(), cellNamespace, listener);  break;
			}
			case VIRTUAL_STATIC_SANDBOX:
			{
				m_cajaWrapper.start_virtualStatic(host, code.getRawCode(), cellNamespace, listener);  break;
			}
		}
	}
	
	public boolean isRunning()
	{
		return m_cajaWrapper.isRunning() || m_iframeSandbox.isRunning();
	}
	
	public void stop()
	{
		if( !isRunning() )
		{
			smU_Debug.ASSERT(false, "smHtmlSandbox::stop1");
			return;
		}
		
		if( m_cajaWrapper.isRunning() )
		{
			m_cajaWrapper.stop();
		}
		else if( m_iframeSandbox.isRunning() )
		{
			m_iframeSandbox.stop();
		}
	}
}
