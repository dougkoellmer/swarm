package b33hive.client.thirdparty.history;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;

public class bhHistoryJsWrapper
{
	public static interface I_Listener
	{
		void onStateChange(String path, JavaScriptObject data);
	}
	
	private final I_Listener m_listener;
	
	public bhHistoryJsWrapper(I_Listener listener)
	{
		m_listener = listener;
		
		initialize(this);
	}
	
	public native void replaceState(String path, String title, JavaScriptObject data)
	/*-{
			var History = $wnd.History;
			History.replaceState(data, title, path);
	}-*/;
	
	public native void pushState(String path, String title, JavaScriptObject data)
	/*-{
			var History = $wnd.History;
			History.pushState(data, title, path);
	}-*/;
	
	public native void go(int offset)
	/*-{
			var History = $wnd.History;
			if( offset > 0 )
			{
				History.forward();
			}
			else if( offset < 0 )
			{
				History.back();
			}
	}-*/;
	
	public native JavaScriptObject getState()
	/*-{
			var History = $wnd.History;
			var State = History.getState();
			
			if( State == null || State.data == null)
			{
				return null;
			}
			else
			{
				return State.data;
			}
	}-*/;

	private void onStateChange(String path, JavaScriptObject data)
	{
		m_listener.onStateChange(path, data);
	}
	
	private static native void initialize(bhHistoryJsWrapper thisArg)
	/*-{
			var History = $wnd.History;
			
			// Bind to StateChange Event
		    History.Adapter.bind
		    (
			    $wnd,
			    'statechange',
			    function()
			    {
			        var State = History.getState();
			        
			        var data = State.data;
			        
			        thisArg.@b33hive.client.thirdparty.history.bhHistoryJsWrapper::onStateChange(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(State.url, data);
			    }
			 );
	}-*/;
}
