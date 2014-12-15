package swarm.client.input;

import java.util.logging.Logger;

import swarm.client.thirdparty.history.HistoryJsWrapper;
import swarm.client.thirdparty.json.GwtJsonObject;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_ReadsJson;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.I_WritesJson;

import com.google.gwt.core.client.JavaScriptObject;

public class BrowserHistoryManager
{
	private static final Logger s_logger = Logger.getLogger(BrowserHistoryManager.class.getName());
	
	public static interface I_Listener
	{
		void onStateChange(String path, I_JsonObject state);
	}
	
	private final HistoryJsWrapper m_historyJs = new HistoryJsWrapper(new HistoryJsWrapper.I_Listener()
	{
		@Override
		public void onStateChange(String path, JavaScriptObject data)
		{
			if( m_performingOperation )  return;
			
			I_JsonObject json = null;
			
			if( data != null )
			{
				json = new GwtJsonObject(BrowserHistoryManager.this.m_jsonFactory, data);
			}
			
			m_listener.onStateChange(path, json);
		}
	});
	
	private boolean m_performingOperation = false;
	
	private final I_Listener m_listener;
	private final A_JsonFactory m_jsonFactory;
	
	public BrowserHistoryManager(A_JsonFactory jsonFactory, I_Listener listener)
	{
		m_jsonFactory = jsonFactory;
		m_listener = listener;
	}
	
	private JavaScriptObject createJsObject(I_WritesJson state)
	{
		if( state == null )  return null;
		
		GwtJsonObject jsonObject = (GwtJsonObject) m_jsonFactory.createJsonObject();
		state.writeJson(jsonObject, m_jsonFactory);
		return jsonObject.getNative().getJavaScriptObject();
	}
	
	public void setState(String path, String title, I_WritesJson state)
	{
//		s_logger.severe(path + " " + state);
//		U_Debug.printStackTrace(8);
		m_performingOperation = true;
		{
			m_historyJs.replaceState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public void pushState(String path, String title, I_WritesJson state)
	{
//		s_logger.severe(path + " " + state);
//		U_Debug.printStackTrace(8);
		m_performingOperation = true;
		{
			m_historyJs.pushState(path, title, createJsObject(state));
		}
		m_performingOperation = false;
	}
	
	public I_JsonObject getCurrentState()
	{
		JavaScriptObject nativeJson = m_historyJs.getState();
		
		if( nativeJson == null )
		{
			return null;
		}
		else
		{
			return new GwtJsonObject(m_jsonFactory, nativeJson);
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
