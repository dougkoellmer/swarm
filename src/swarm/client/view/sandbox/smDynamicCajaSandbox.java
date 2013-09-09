package swarm.client.view.sandbox;

import swarm.client.entities.smBufferCell;
import swarm.client.view.tabs.code.smI_CodeLoadListener;
import swarm.shared.code.smU_UriPolicy;
import swarm.shared.code.smUriData;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.structs.smCodePrivileges;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

public class smDynamicCajaSandbox implements smI_CellSandbox
{
	private static final smUriData s_utilUriData = new smUriData();
	
	private final smCellApi m_cellApi;
	private final String m_apiNamespace;
	
	smDynamicCajaSandbox(smCellApi cellApi, String apiNamespace)
	{
		m_cellApi = cellApi;
		m_apiNamespace = apiNamespace;
	}
	
	private String rewriteUri(String fullUri, String scheme, String authority, String attribute, String path)
	{
		s_utilUriData.attribute = attribute;
		s_utilUriData.authority = authority;
		s_utilUriData.client = true;
		s_utilUriData.fullUri = fullUri;
		s_utilUriData.path = path;
		s_utilUriData.scheme = scheme;
		
		//--- DRK > Something of a hack here so that the rewriter can correctly identify "snappable" relative paths.
		if( authority!= null && s_utilUriData.authority.equalsIgnoreCase(Window.Location.getHostName()) )
		{
			s_utilUriData.authority = null;
			s_utilUriData.scheme = null;
		}
		
		smBufferCell cell = m_cellApi.getCurrentCell();
		smCodePrivileges privileges = cell.getCodePrivileges();
		
		if( privileges == null )
		{
			smU_Debug.ASSERT(false, "Expected privileges to not be null.");
			
			privileges = new smCodePrivileges();
		}

		String newUri = smU_UriPolicy.rewriteUri(privileges.getNetworkPrivilege(), s_utilUriData, m_apiNamespace, null);
		
		return newUri;
	}
	

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
	
	private native void start_native(Element host, String rawCode, String cellNamespace, smI_CodeLoadListener listener)
	/*-{
			var thisArg = this;
			
			function uriPolicyHelper(uri)
			{
				var attribute = arguments[3]["XML_TAG"] + "::" + arguments[3]["XML_ATTR"];
				var authority = uri.domain_;
				var path = uri.path_;
				var scheme = uri.scheme_;
				var fullUri = uri.toString();
				
				var rewrittenUri = thisArg.@swarm.client.view.sandbox.smDynamicCajaSandbox::rewriteUri(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(fullUri, scheme, authority, attribute, path);
				
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
				host,
				uriPolicy,
				
				function(frame)
				{
					thisArg.@swarm.client.view.sandbox.smDynamicCajaSandbox::onFrameLoad(Lcom/google/gwt/core/client/JavaScriptObject;)(frame);

	   				frame.code('', 'text/html', compiledHtml)
	   					 .api(makeApi())
	   					 .run();
	   				
	   				if( listener != null )
	   				{
	   					listener.@swarm.client.view.tabs.code.smI_CodeLoadListener::onCodeLoad()();
	   				}
				},
				{
					idClass:cellNamespace
				}
			);
	}-*/;

	@Override
	public void start(Element host, String rawCode, String cellNamespace, smI_CodeLoadListener listener)
	{
		start_native(host, rawCode, cellNamespace, listener);
	}

	@Override
	public void stop(Element host)
	{
		
	}
}
