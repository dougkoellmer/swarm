package b33hive.client.navigation;

import b33hive.client.input.bhBrowserHistoryManager;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhPoint;

public class bhHistoryStateManager
{
	public static interface I_Listener
	{
		void onStateChange(String path, bhHistoryState state);
	}
	
	private final bhBrowserHistoryManager m_historyManager = new bhBrowserHistoryManager(new bhBrowserHistoryManager.I_Listener()
	{
		@Override
		public void onStateChange(String path, bhI_JsonObject json)
		{
			bhHistoryState state = null;
			
			if( json != null )
			{
				state = new bhHistoryState();
				state.readJson(json);
			}
			
			m_listener.onStateChange(path, state);
		}
	});
	
	private final I_Listener m_listener;
	
	public bhHistoryStateManager(I_Listener listener)
	{
		m_listener = listener;
	}
	
	public void setState(String path, bhPoint point)
	{
		this.setState(path, new bhHistoryState(point));
	}
	
	public void setState(String path, bhCellAddressMapping mapping)
	{
		this.setState(path, new bhHistoryState(mapping));
	}
	
	public void setState(bhCellAddress address, bhHistoryState state)
	{
		this.setState(address.getRawAddress(), state);
	}
	
	private static String getTitleFromPath(String path)
	{
		if( path.equals("/") || path == null || path.isEmpty() )
		{
			return "This is b33hive.";
		}
		else
		{
			return path;
		}
	}
	
	public void setState(String path, bhHistoryState state)
	{
		path = path.startsWith("/") ? path : "/" + path;
		
		m_historyManager.setState(path, getTitleFromPath(path), state);
	}
	
	public void pushState(bhCellAddress address, bhHistoryState state)
	{
		this.pushState(address.getRawAddress(), state);
	}
	
	public void pushState(String path, bhPoint point)
	{
		this.pushState(path, new bhHistoryState(point));
	}
	
	public void pushState(String path, bhCellAddressMapping mapping)
	{
		this.pushState(path, new bhHistoryState(mapping));
	}
	
	public void pushState(String path, bhHistoryState state)
	{
		bhHistoryState currentState = this.getCurrentState();
		
		if( currentState != null )
		{
			if( state.isEqualTo(currentState) )
			{
				return; // Just a user experience thing...I think it's weird for the back button to just refresh a cell and not move you.
			}
		}
		
		path = path.startsWith("/") ? path : "/" + path;
		m_historyManager.pushState(path, getTitleFromPath(path), state);
	}
	
	public bhHistoryState getCurrentState()
	{
		bhI_JsonObject json = m_historyManager.getCurrentState();
		
		if( json != null )
		{
			bhHistoryState state = new bhHistoryState();
			state.readJson(json);
			
			return state;
		}
		else
		{
			return null;
		}
	}
	
	public void go(int offset)
	{
		m_historyManager.go(offset);
	}
}
