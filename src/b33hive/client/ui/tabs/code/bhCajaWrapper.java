package b33hive.client.ui.tabs.code;

import java.util.HashMap;

import org.apache.commons.collections.map.HashedMap;

import b33hive.client.entities.bhBufferCell;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.ui.tabs.code.bhCellSandbox.I_StartUpCallback;
import b33hive.shared.code.bhU_UriPolicy;
import b33hive.shared.code.bhUriData;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.memory.bhObjectPool;
import b33hive.shared.reflection.bhI_Class;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhE_NetworkPrivilege;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.thirdparty.bhS_Caja;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

//TODO: There are a lot of references to b33hive-specific code as far as the API given to sandboxed code.
public class bhCajaWrapper
{	
	private HashMap<Element, bhStaticCajaContainer> m_associations = new HashMap<Element, bhStaticCajaContainer>();
	
	private final I_StartUpCallback m_callback;
	
	private Element m_currentHostElement = null;
	
	private final String m_apiNamespace;
	
	private final bhUriData m_utilUriData = new bhUriData();
	
	bhCajaWrapper(I_StartUpCallback callback, String apiNamespace)
	{
		m_apiNamespace = apiNamespace;
		
		m_callback = callback;
		
		bhU_CellApi.registerApi(apiNamespace);
		
		initialize_native(this, apiNamespace);
	}
	
	static native void tameApi(String apiNamespace)
	/*-{
		{// BH SPECIFIC
			$wnd.caja.markReadOnlyRecord($wnd[apiNamespace]);
			
			$wnd.caja.markFunction($wnd[apiNamespace].snap);
			
			$wnd.caja.markFunction($wnd[apiNamespace].getCoordinate);
			$wnd.caja.markFunction($wnd[apiNamespace].getAddress);
			$wnd.caja.markFunction($wnd[apiNamespace].getPosition);
			$wnd.caja.markFunction($wnd[apiNamespace].getUsername);
			$wnd.caja.markFunction($wnd[apiNamespace].getGridWidth);
			$wnd.caja.markFunction($wnd[apiNamespace].getGridHeight);
			
			$wnd[apiNamespace+"_tamed"] = $wnd.caja.tame($wnd[apiNamespace]);
		}

		{// CONSOLE
			var consoleWrapper;
			if (!"console" in window || typeof console === "undefined")
			{
				//TODO: Define an alternate logging facility, maybe within b33hive itself
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
	
	private native void initialize_native(bhCajaWrapper thisArg, String apiNamespace)
	/*-{
			$wnd.caja.initFeralFrame(window);
			
			function onSuccess()
			{
				$wnd.caja.whenReady
				(
					function()
					{
						@b33hive.client.ui.tabs.code.bhCajaWrapper::tameApi(Ljava/lang/String;)(apiNamespace);
						thisArg.@b33hive.client.ui.tabs.code.bhCajaWrapper::caja_initialize_success()();
					}
				);
			}
		
			$wnd.caja.initialize
			(
				{
					cajaServer:				'/r.js/caja/2/',
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
					thisArg.@b33hive.client.ui.tabs.code.bhCajaWrapper::caja_initialize_failure()();
				}
			);
	}-*/;
	
	//--- DRK > NOTE: Assuming here that caja.load creates frames in order of calls...if not, this method could be trouble.
	private void onFrameLoad(JavaScriptObject cajaFrameObject)
	{
		//--- This is to catch the theoretical case where we call start and stop then start really fast,
		//--- but the load call from the first start didn't finish before the first stop, and comes in between
		//--- the first stop and the second start...NOTE this is just done based on my thoughts on possible fringe
		//--- cases, not because I actually noticed this behavior.
		//this.stop();
		
		//m_cajaFrameObjects.push(cajaFrameObject);
	}
	
	private bhStaticCajaContainer getContainer(Element element)
	{
		bhStaticCajaContainer cajaContainer = m_associations.get(element);
		
		if( cajaContainer == null )
		{
			cajaContainer = new bhStaticCajaContainer(element);
			m_associations.put(element, cajaContainer);
		}
		return cajaContainer;
	}
	
	public void allowScrolling(Element element, boolean yesOrNo)
	{
		bhStaticCajaContainer cajaContainer = getContainer(element);
		cajaContainer.allowScrolling(yesOrNo);
	}
	
	public void insertStaticHtml(Element element, String compiledHtml, String cellNamespace)
	{
		bhStaticCajaContainer cajaContainer = getContainer(element);
		
		cellNamespace += bhS_Caja.CAJA_NAMESPACE_SUFFIX;
		
		cajaContainer.setCellNamespace(cellNamespace);
		
		cajaContainer.setInnerHtml(compiledHtml);
	}
	
	public void start(Element hostElement, String compiledHtml, String compiledJs, String cellNamespace, bhI_CodeLoadListener listener)
	{
		if( m_currentHostElement != null )
		{
			this.stop();
		}
		
		m_currentHostElement = hostElement;
		
		bhStaticCajaContainer cajaContainer = getContainer(hostElement);
		
		if( cajaContainer != null )
		{
			cajaContainer.detachIfNecessary();
		}
		
		// HACK: namespacing dynamic content separately to avoid "leaked" setInterval calls being able to
		//		modify static DOM elements that are namespaced normally without the prefix. This is pretty
		//		much just a bandaid workaround...behind the scenes, user code may still try to access non-existent elements.
		cellNamespace = "d_" + cellNamespace;
		
		native_start(this, hostElement, compiledHtml, compiledJs, cellNamespace, listener, m_apiNamespace);
	}
	
	private String rewriteUri(String fullUri, String scheme, String authority, String attribute, String path)
	{
		m_utilUriData.attribute = attribute;
		m_utilUriData.authority = authority;
		m_utilUriData.client = true;
		m_utilUriData.fullUri = fullUri;
		m_utilUriData.path = path;
		m_utilUriData.scheme = scheme;
		
		//--- DRK > Something of a hack here so that the rewriter can correctly identify "snappable" relative paths.
		if( authority!= null && m_utilUriData.authority.equalsIgnoreCase(Window.Location.getHostName()) )
		{
			m_utilUriData.authority = null;
			m_utilUriData.scheme = null;
		}
		
		bhBufferCell cell = bhU_CellApi.getCurrentCell();
		bhCodePrivileges privileges = cell.getCodePrivileges();
		
		if( privileges == null )
		{
			bhU_Debug.ASSERT(false, "Expected privileges to not be null.");
			
			privileges = new bhCodePrivileges();
		}
		
		String newUri = bhU_UriPolicy.rewriteUri(privileges.getNetworkPrivilege(), m_utilUriData, null);
		
		return newUri;
	}
	
	private native void native_start(bhCajaWrapper thisArg, Element element, String compiledHtml, String compiledJs, String cellNamespace, bhI_CodeLoadListener listener, String apiNamespace)
	/*-{
			function uriPolicyHelper(uri)
			{
				var attribute = arguments[3]["XML_TAG"] + "::" + arguments[3]["XML_ATTR"];
				var authority = uri.domain_;
				var path = uri.path_;
				var scheme = uri.scheme_;
				var fullUri = uri.toString();
				
				var rewrittenUri = thisArg.@b33hive.client.ui.tabs.code.bhCajaWrapper::rewriteUri(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(fullUri, scheme, authority, attribute, path);
				
				return rewrittenUri;
			}
			
			var uriPolicy =
			{
				rewrite: function(uri)
				{
					return uriPolicyHelper.apply(null , arguments);
				},
				fetch: function xhrFetcher(url, mime, callback)
				{
					//console.log(mime);
				    var request = new XMLHttpRequest();
				    request.open('GET', url.toString(), true);
				    if( request.overrideMimeType)  request.overrideMimeType(mime);
				    request.onreadystatechange = function() {
				      if(request.readyState == 4) {
				        callback({ "html": request.responseText });
				      }
				    };
				    request.send();
				},
				mitigate: function (uri)
				{
		            // Skip rewriting jquery and jqueryui when loaded
		            // from the google cdn
		            if (uri.getDomain() === "ajax.googleapis.com" &&
		                (uri.getPath().indexOf("/ajax/libs/jquery/") === 0 ||
		                 uri.getPath().indexOf("/ajax/libs/jqueryui/") === 0))  {
		              return uri;
		            }
		            return null;
		        }
			};
			
			function makeApi()
			{
				var api =
				{
					console:$wnd.console_tamed,
   					alert:$wnd.alert_tamed
				};
				
				api[apiNamespace] = $wnd[apiNamespace+"_tamed"];
				
				return api;
			}

			$wnd.caja.load
			(
				element,
				uriPolicy,
				
				function(frame)
				{
					thisArg.@b33hive.client.ui.tabs.code.bhCajaWrapper::onFrameLoad(Lcom/google/gwt/core/client/JavaScriptObject;)(frame);

	   				frame.code('', 'text/html', compiledHtml)
	   					 .api(mapeApi())
	   					 .run();
	   				
	   				if( listener != null )
	   				{
	   					listener.@b33hive.client.ui.tabs.code.bhI_CodeLoadListener::onCodeLoad()();
	   				}
				},
				{
					idClass:cellNamespace
				}
			);
	}-*/;
	
	public void stop()
	{
		if( m_currentHostElement != null )
		{
			Node child = m_currentHostElement.getChild(0);

			if( child != null )
			{
				m_currentHostElement.removeChild(child);
			}
			
			m_currentHostElement = null;
		}
	}
}
