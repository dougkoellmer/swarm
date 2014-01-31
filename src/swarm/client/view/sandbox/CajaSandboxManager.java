package swarm.client.view.sandbox;

import java.util.HashMap;

import org.apache.commons.collections.map.HashedMap;

import swarm.client.entities.BufferCell;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.view.ViewContext;
import swarm.client.view.sandbox.SandboxManager.I_StartUpCallback;
import swarm.client.view.tabs.code.I_CodeLoadListener;
import swarm.shared.code.U_UriPolicy;
import swarm.shared.code.smUriData;
import swarm.shared.debugging.U_Debug;
import swarm.shared.memory.ObjectPool;
import swarm.shared.reflection.I_Class;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_NetworkPrivilege;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.thirdparty.S_Caja;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;


public class CajaSandboxManager
{
	private HashMap<Element, I_CellSandbox> m_associations = new HashMap<Element, I_CellSandbox>();
	
	private final I_StartUpCallback m_callback;
	
	private final String m_apiNamespace;
	
	private final CellApi m_cellApi;
	
	private final ObjectPool<StaticCajaSandbox> m_staticSandboxPool;
	private final ObjectPool<DynamicCajaSandbox> m_dynamicSandboxPool;
	
	CajaSandboxManager(final CellApi cellApi, I_StartUpCallback callback, final String apiNamespace, boolean useDynamicSandbox)
	{
		m_cellApi = cellApi;
		m_apiNamespace = apiNamespace;
		m_callback = callback;
		
		m_staticSandboxPool = new ObjectPool<StaticCajaSandbox>(new I_Class<StaticCajaSandbox>()
		{
			@Override
			public StaticCajaSandbox newInstance()
			{
				return new StaticCajaSandbox();
			}
		});
		
		if( useDynamicSandbox )
		{
			m_dynamicSandboxPool = new ObjectPool<DynamicCajaSandbox>(new I_Class<DynamicCajaSandbox>()
			{
				@Override
				public DynamicCajaSandbox newInstance()
				{
					return new DynamicCajaSandbox(cellApi, apiNamespace);
				}
			});
			
			initialize_native(this, apiNamespace);
		}
		else
		{
			m_dynamicSandboxPool = null;
			
			m_callback.onStartUpComplete(true);
		}
	}
	
	private void createApi(String apiNamespace)
	{
		tameApi(apiNamespace);
	}
	
	private native void tameApi(String apiNamespace)
	/*-{
		{
			$wnd.caja.markReadOnlyRecord($wnd[apiNamespace]);
			
			$wnd.caja.markFunction($wnd[apiNamespace].snap);
			
			$wnd.caja.markFunction($wnd[apiNamespace].getCoordinate);
			$wnd.caja.markFunction($wnd[apiNamespace].getAddress);
			$wnd.caja.markFunction($wnd[apiNamespace].getPosition);
			$wnd.caja.markFunction($wnd[apiNamespace].getUsername);
			$wnd.caja.markFunction($wnd[apiNamespace].getGridWidth);
			$wnd.caja.markFunction($wnd[apiNamespace].getGridHeight);
			$wnd.caja.markFunction($wnd[apiNamespace].getCellWidth);
			$wnd.caja.markFunction($wnd[apiNamespace].getCellHeight);
			$wnd.caja.markFunction($wnd[apiNamespace].getCellPadding);
			
			$wnd[apiNamespace+"_tamed"] = $wnd.caja.tame($wnd[apiNamespace]);
		}

		{// CONSOLE
			var consoleWrapper;
			if (!"console" in window || typeof console === "undefined")
			{
				//TODO: Define an alternate logging facility, maybe within swarm itself
				consoleWrapper =
				{
					log:function(arg){}
				};
			}
			else
			{
				consoleWrapper =
				{
					log:function(arg){console.log(arg);}
				};
			}
			$wnd.caja.markReadOnlyRecord(consoleWrapper);
			$wnd.caja.markFunction(consoleWrapper.log);
			$wnd.console_tamed = $wnd.caja.tame(consoleWrapper);
		}
		
		{// ALERT
			
			$wnd.caja.markFunction($wnd[apiNamespace+"_alert"]);
			$wnd.alert_tamed = $wnd.caja.tame($wnd[apiNamespace+"_alert"]);
		}
	}-*/;
	
	private void caja_initialize_success()
	{
		m_callback.onStartUpComplete(true);
	}
	
	private void caja_initialize_failure()
	{
		m_callback.onStartUpComplete(false);
	}
	
	private native void initialize_native(CajaSandboxManager thisArg, String apiNamespace)
	/*-{
			$wnd.caja.initFeralFrame(window);
			
			function onSuccess()
			{
				$wnd.caja.whenReady
				(
					function()
					{
						thisArg.@swarm.client.view.sandbox.CajaSandboxManager::createApi(Ljava/lang/String;)(apiNamespace);
						thisArg.@swarm.client.view.sandbox.CajaSandboxManager::caja_initialize_success()();
					}
				);
			}
		
			$wnd.caja.initialize
			(
				{
					server:					'/r.js/caja/2/',
					resources:				'/r.js/caja/2/',
					es5Mode :				true,
					debug:					false,
					maxAcceptableSeverity:	"NO_KNOWN_EXPLOIT_SPEC_VIOLATION"
				},
				function() //on success
				{
					onSuccess();
				},
				function() // on failure
				{
					thisArg.@swarm.client.view.sandbox.CajaSandboxManager::caja_initialize_failure()();
				}
			);
	}-*/;
	
	private I_CellSandbox getSandbox(Element host)
	{
		I_CellSandbox sandbox = m_associations.get(host);
		
		return sandbox;
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		//smStaticCajaSandbox cajaContainer = getContainer(element);
		
		//cajaContainer.allowScrolling(yesOrNo);
	}
	
	void start_static(Element host, String rawCode, String cellNamespace, I_CodeLoadListener listener)
	{
		I_CellSandbox currentSandbox = this.getSandbox(host);
		
		if( currentSandbox != null )
		{
			currentSandbox.stop(host);
		}
		
		cellNamespace += S_Caja.CAJA_NAMESPACE_SUFFIX;
		
		if( currentSandbox instanceof StaticCajaSandbox )
		{
			currentSandbox.start(host, rawCode, cellNamespace, listener);
		}
		else
		{
			if( currentSandbox != null )
			{
				this.deallocate(host, currentSandbox);
			}
			
			currentSandbox = this.allocate(host, StaticCajaSandbox.class);
			currentSandbox.start(host, rawCode, cellNamespace, listener);
		}
	}
	
	private I_CellSandbox allocate(Element host, Class<? extends I_CellSandbox> T)
	{
		I_CellSandbox toReturn = null;
		
		if( T == StaticCajaSandbox.class )
		{
			toReturn = m_staticSandboxPool.allocate();
		}
		else
		{
			toReturn = m_dynamicSandboxPool.allocate();
		}
		
		m_associations.put(host, toReturn);
		
		return toReturn;
	}
	
	private void deallocate(Element host, I_CellSandbox instance)
	{
		if( instance.getClass() == StaticCajaSandbox.class )
		{
			m_staticSandboxPool.deallocate((StaticCajaSandbox) instance);
		}
		else
		{
			m_dynamicSandboxPool.deallocate((DynamicCajaSandbox) instance);
		}
		
		m_associations.remove(host);
	}
	
	void start_dynamic(Element host, String rawCode, String cellNamespace, I_CodeLoadListener listener)
	{
		if( m_dynamicSandboxPool == null )
		{
			this.start_static(host, "Dynamic sandbox not available.", cellNamespace, listener);
			return;
		}
		
		I_CellSandbox currentSandbox = this.getSandbox(host);
		
		if( currentSandbox != null )
		{
			currentSandbox.stop(host);
		}
		
		// HACK: namespacing dynamic content separately to avoid "leaked" setInterval calls, et al, being able to
		//		modify static DOM elements that are namespaced normally without the prefix. This is pretty
		//		much just a bandaid workaround...behind the scenes, user code may still try to access non-existent elements.
		cellNamespace = "d_" + cellNamespace + S_Caja.CAJA_NAMESPACE_SUFFIX;
		
		if( currentSandbox instanceof DynamicCajaSandbox )
		{
			currentSandbox.start(host, rawCode, cellNamespace, listener);
		}
		else
		{
			if( currentSandbox != null )
			{
				this.deallocate(host, currentSandbox);
			}
			
			currentSandbox = this.allocate(host, DynamicCajaSandbox.class);
			currentSandbox.start(host, rawCode, cellNamespace, listener);
		}
	}
	
	void stop(Element host)
	{
		I_CellSandbox currentSandbox = this.getSandbox(host);
		
		if( currentSandbox != null )
		{
			currentSandbox.stop(host);
			
			this.deallocate(host, currentSandbox);
		}
	}
}
