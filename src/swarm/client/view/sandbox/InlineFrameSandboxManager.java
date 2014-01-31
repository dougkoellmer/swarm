package swarm.client.view.sandbox;

import java.util.HashMap;

import swarm.client.view.tabs.code.I_CodeLoadListener;
import swarm.shared.debugging.U_Debug;
import swarm.shared.memory.ObjectPool;
import swarm.shared.reflection.I_Class;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Element;

public class InlineFrameSandboxManager
{
	private HashMap<Element, InlineFrameSandbox> m_associations = new HashMap<Element, InlineFrameSandbox>();
	
	private final ObjectPool<InlineFrameSandbox> m_sandboxPool;
	
	public InlineFrameSandboxManager(final String apiNamespace)
	{
		m_sandboxPool = new ObjectPool<InlineFrameSandbox>(new I_Class<InlineFrameSandbox>()
		{
			@Override
			public InlineFrameSandbox newInstance()
			{
				return new InlineFrameSandbox(apiNamespace);
			}
		});
	}
	
	private InlineFrameSandbox getSandbox(Element host)
	{
		return m_associations.get(host);
	}
	
	private InlineFrameSandbox allocateSandbox(Element host)
	{
		InlineFrameSandbox instance = m_sandboxPool.allocate();
		
		m_associations.put(host, instance);
		
		return instance;
	}
	
	private void deallocateSandbox(Element host, InlineFrameSandbox sandbox)
	{
		m_sandboxPool.deallocate(sandbox);
		
		m_associations.remove(host);
	}
	
	private InlineFrameSandbox start_helper(Element host)
	{
		InlineFrameSandbox sandbox = this.getSandbox(host);
		
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
	
	void start_local(Element host, String rawCode, I_CodeLoadListener listener)
	{
		InlineFrameSandbox sandbox = this.start_helper(host);
		
		sandbox.start_local(host, rawCode, listener);
	}
	
	void start_remote(Element host, String src, I_CodeLoadListener listener)
	{
		InlineFrameSandbox sandbox = this.start_helper(host);
		
		sandbox.start_remote(host, src, listener);
	}
	
	void stop(Element host)
	{
		InlineFrameSandbox sandbox = this.getSandbox(host);
		
		if( sandbox != null )
		{
			sandbox.stop(host);
			this.deallocateSandbox(host, sandbox);
		}
	}
}
