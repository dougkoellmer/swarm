package swarm.client.input;

import swarm.client.thirdparty.history.smHistoryJsWrapper;
import swarm.client.thirdparty.json.smGwtJsonObject;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_ReadsJson;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smI_WritesJson;

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
				json = new smGwtJsonObject(smBrowserHistoryManager.this.m_jsonFactory, data);
			}
			
			m_listener.onStateChange(path, json);
		}
	});
	
	private boolean m_performingOperation = false;
	
	private final I_Listener m_listener;
	private final smA_JsonFactory m_jsonFactory;
	
	public smBrowserHistoryManager(smA_JsonFactory jsonFactory, I_Listener listener)
	{
		m_jsonFactory = jsonFactory;
		m_listener = listener;
	}
	
	private JavaScriptObject createJsObject(smI_WritesJson state)
	{
		if( state == null )  return null;
		
		smGwtJsonObject jsonObject = (smGwtJsonObject) m_jsonFactory.createJsonObject();
		state.writeJson(m_jsonFactory, jsonObject);
		return jsonObject.getNative().getJavaScriptObject();
	}
	
	public void setState(String path, String title, smI_WritesJson state)
	{
		m_performingOperation = true;
		{
			m_historyJs.replaceState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public void pushState(String path, String title, smI_WritesJson state)
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
			return new smGwtJsonObject(m_jsonFactory, nativeJson);
		}
	}
	
	public boolean hasState(int offset)
	{
		return m_historyJs.hasState(offset);
	}
	
	public void go(int offset)
	{
		m_historyJs.go(offset);
	}
}
