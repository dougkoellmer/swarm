package swarm.client.view.sandbox;

import java.util.HashMap;

import swarm.client.view.tabs.code.smI_CodeLoadListener;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.memory.smObjectPool;
import swarm.shared.reflection.smI_Class;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Element;

public class smInlineFrameSandboxManager
{
	private HashMap<Element, smInlineFrameSandbox> m_associations = new HashMap<Element, smInlineFrameSandbox>();
	
	private final smObjectPool<smInlineFrameSandbox> m_sandboxPool;
	
	public smInlineFrameSandboxManager(final String apiNamespace)
	{
		m_sandboxPool = new smObjectPool<smInlineFrameSandbox>(new smI_Class<smInlineFrameSandbox>()
		{
			@Override
			public smInlineFrameSandbox newInstance()
			{
				return new smInlineFrameSandbox(apiNamespace);
			}
		});
	}
	
	private smInlineFrameSandbox getSandbox(Element host)
	{
		return m_associations.get(host);
	}
	
	private smInlineFrameSandbox allocateSandbox(Element host)
	{
		smInlineFrameSandbox instance = m_sandboxPool.allocate();
		
		m_associations.put(host, instance);
		
		return instance;
	}
	
	private void deallocateSandbox(Element host, smInlineFrameSandbox sandbox)
	{
		m_sandboxPool.deallocate(sandbox);
		
		m_associations.remove(host);
	}
	
	private smInlineFrameSandbox start_helper(Element host)
	{
		smInlineFrameSandbox sandbox = this.getSandbox(host);
		
		if( sandbox != null )
		{
			sandbox.stop(host);
		}
		else
		{
			sandbox = this.allocateSandbox(host);
		}
		
		return sandbox;
	}
	
	void start_local(Element host, String rawCode, smI_CodeLoadListener listener)
	{
		smInlineFrameSandbox sandbox = this.start_helper(host);
		
		sandbox.start_local(host, rawCode, listener);
	}
	
	void start_remote(Element host, String src, smI_CodeLoadListener listener)
	{
		smInlineFrameSandbox sandbox = this.start_helper(host);
		
		sandbox.start_remote(host, src, listener);
	}
	
	void stop(Element host)
	{
		smInlineFrameSandbox sandbox = this.getSandbox(host);
		
		if( sandbox != null )
		{
			sandbox.stop(host);
			this.deallocateSandbox(host, sandbox);
		}
	}
}
