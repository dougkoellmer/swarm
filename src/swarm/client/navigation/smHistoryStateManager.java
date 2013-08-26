package swarm.client.navigation;

import swarm.client.input.smBrowserHistoryManager;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smPoint;

public class smHistoryStateManager
{
	public static interface I_Listener
	{
		void onStateChange(String path, smHistoryState state);
	}
	
	private final smBrowserHistoryManager m_historyManager = new smBrowserHistoryManager(new smBrowserHistoryManager.I_Listener()
	{
		@Override
		public void onStateChange(String path, smI_JsonObject json)
		{
			smHistoryState state = null;
			
			if( json != null )
			{
				state = new smHistoryState();
				state.readJson(json);
			}
			
			m_listener.onStateChange(path, state);
		}
	});
	
	private final I_Listener m_listener;
	private final String m_defaultPageTitle;
	
	public smHistoryStateManager(String defaultPageTitle, I_Listener listener)
	{
		m_defaultPageTitle = defaultPageTitle;
		m_listener = listener;
	}
	
	public void setState(String path, smPoint point)
	{
		this.setState(path, new smHistoryState(point));
	}
	
	public void setState(String path, smCellAddressMapping mapping)
	{
		this.setState(path, new smHistoryState(mapping));
	}
	
	public void setState(smCellAddress address, smHistoryState state)
	{
		this.setState(address.getCasedRawAddressLeadSlash(), state);
	}
	
	public void setState(smCellAddress address, smCellAddressMapping mapping)
	{
		this.setState(address, new smHistoryState(mapping));
	}
	
	private String getTitleFromPath(String path)
	{
		if( path.equals("/") || path == null || path.isEmpty() )
		{
			return m_defaultPageTitle;
		}
		else
		{
			return path;
		}
	}
	
	public void setState(String path, smHistoryState state)
	{
		path = path.startsWith("/") ? path : "/" + path;
		
		m_historyManager.setState(path, getTitleFromPath(path), state);
	}
	
	public void pushState(smCellAddress address, smHistoryState state)
	{
		this.pushState(address.getCasedRawAddressLeadSlash(), state);
	}
	
	public void pushState(String path, smPoint point)
	{
		this.pushState(path, new smHistoryState(point));
	}
	
	public void pushState(String path, smCellAddressMapping mapping)
	{
		this.pushState(path, new smHistoryState(mapping));
	}
	
	public void pushState(smCellAddress address, smCellAddressMapping mapping)
	{
		this.pushState(address, new smHistoryState(mapping));
	}
	
	public void pushState(String path, smHistoryState state)
	{
		smHistoryState currentState = this.getCurrentState();
		
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
	
	public smHistoryState getCurrentState()
	{
		smI_JsonObject json = m_historyManager.getCurrentState();
		
		if( json != null )
		{
			smHistoryState state = new smHistoryState();
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
