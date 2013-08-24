package swarm.client.input;

import swarm.client.thirdparty.history.bhHistoryJsWrapper;
import swarm.client.thirdparty.json.bhGwtJsonObject;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonEncodable;
import swarm.shared.json.bhI_JsonObject;
import com.google.gwt.core.client.JavaScriptObject;

public class bhBrowserHistoryManager
{
	public static interface I_Listener
	{
		void onStateChange(String path, bhI_JsonObject state);
	}
	
	private final bhHistoryJsWrapper m_historyJs = new bhHistoryJsWrapper(new bhHistoryJsWrapper.I_Listener()
	{
		@Override
		public void onStateChange(String path, JavaScriptObject data)
		{
			if( m_performingOperation )  return;
			
			bhI_JsonObject json = null;
			
			if( data != null )
			{
				json = new bhGwtJsonObject(data);
			}
			
			m_listener.onStateChange(path, json);
		}
	});
	
	private boolean m_performingOperation = false;
	
	private final I_Listener m_listener;
	
	public bhBrowserHistoryManager(I_Listener listener)
	{
		m_listener = listener;
	}
	
	private static JavaScriptObject createJsObject(bhI_JsonEncodable state)
	{
		return state != null ? ((bhGwtJsonObject)state.writeJson()).getNative().getJavaScriptObject() : null;
	}
	
	public void setState(String path, String title, bhI_JsonEncodable state)
	{
		m_performingOperation = true;
		{
			m_historyJs.replaceState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public void pushState(String path, String title, bhI_JsonEncodable state)
	{
		m_performingOperation = true;
		{
			m_historyJs.pushState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public bhI_JsonObject getCurrentState()
	{
		JavaScriptObject nativeJson = m_historyJs.getState();
		
		if( nativeJson == null )
		{
			return null;
		}
		else
		{
			return new bhGwtJsonObject(nativeJson);
		}
	}
	
	public void go(int offset)
	{
		m_historyJs.go(offset);
	}
}
