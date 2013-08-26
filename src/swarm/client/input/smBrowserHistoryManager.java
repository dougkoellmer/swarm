package swarm.client.input;

import swarm.client.thirdparty.history.smHistoryJsWrapper;
import swarm.client.thirdparty.json.smGwtJsonObject;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonEncodable;
import swarm.shared.json.smI_JsonObject;
import com.google.gwt.core.client.JavaScriptObject;

public class smBrowserHistoryManager
{
	public static interface I_Listener
	{
		void onStateChange(String path, smI_JsonObject state);
	}
	
	private final smHistoryJsWrapper m_historyJs = new smHistoryJsWrapper(new smHistoryJsWrapper.I_Listener()
	{
		@Override
		public void onStateChange(String path, JavaScriptObject data)
		{
			if( m_performingOperation )  return;
			
			smI_JsonObject json = null;
			
			if( data != null )
			{
				json = new smGwtJsonObject(data);
			}
			
			m_listener.onStateChange(path, json);
		}
	});
	
	private boolean m_performingOperation = false;
	
	private final I_Listener m_listener;
	
	public smBrowserHistoryManager(I_Listener listener)
	{
		m_listener = listener;
	}
	
	private static JavaScriptObject createJsObject(smI_JsonEncodable state)
	{
		return state != null ? ((smGwtJsonObject)state.writeJson()).getNative().getJavaScriptObject() : null;
	}
	
	public void setState(String path, String title, smI_JsonEncodable state)
	{
		m_performingOperation = true;
		{
			m_historyJs.replaceState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public void pushState(String path, String title, smI_JsonEncodable state)
	{
		m_performingOperation = true;
		{
			m_historyJs.pushState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public smI_JsonObject getCurrentState()
	{
		JavaScriptObject nativeJson = m_historyJs.getState();
		
		if( nativeJson == null )
		{
			return null;
		}
		else
		{
			return new smGwtJsonObject(nativeJson);
		}
	}
	
	public void go(int offset)
	{
		m_historyJs.go(offset);
	}
}
