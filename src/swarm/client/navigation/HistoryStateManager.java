package swarm.client.navigation;

import java.util.logging.Logger;

import swarm.client.input.BrowserAddressManager;
import swarm.client.input.BrowserHistoryManager;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.Point;

public class HistoryStateManager
{
	private static final Logger s_logger = Logger.getLogger(HistoryStateManager.class.getName());
	
	public static interface I_Listener
	{
		void onStateChange(String url, HistoryState state);
	}
	
	private final BrowserAddressManager m_addressMngr;
	private final BrowserHistoryManager m_historyManager;
	
	private final I_Listener m_listener;
	private final String m_defaultPageTitle;
	private final A_JsonFactory m_jsonFactory;
	
	private int m_currentId = 0;
	private int m_highestId = -1;
	
	public HistoryStateManager(A_JsonFactory jsonFactory, String defaultPageTitle, I_Listener listener, BrowserAddressManager addressMngr)
	{
		m_jsonFactory = jsonFactory;
		m_defaultPageTitle = defaultPageTitle;
		m_listener = listener;
		m_addressMngr = addressMngr;
		
		m_historyManager = new BrowserHistoryManager(jsonFactory, new BrowserHistoryManager.I_Listener()
		{
			@Override
			public void onStateChange(String url, I_JsonObject json)
			{
				HistoryState state = null;
				
				if( json != null )
				{
					state = new HistoryState();
					state.readJson(json, m_jsonFactory);
					
					int id = state.getId();
					
					//s_logger.severe("state id: " + id + ", currentId: " + m_currentId);
					
					HistoryStateManager.this.setCurrentId(id);
					
					//--- DRK > Resave the state so accurate highestId is preserved if user navigates away from page.
					String justThePath = m_addressMngr.getCurrentPath();
					HistoryStateManager.this.setState(justThePath, state);
				}
				
				m_listener.onStateChange(url, state);
			}
		});
	}
	
	private void setCurrentId(int id)
	{
		m_currentId = id;
		m_highestId = m_currentId > m_highestId ? m_currentId : m_highestId;
	}
	
	public void setState(String path, Point point)
	{
		this.setState(path, new HistoryState(point));
	}
	
	public void setState(String path, CellAddressMapping mapping)
	{
		this.setState(path, new HistoryState(mapping));
	}
	
	public void setState(CellAddress address, HistoryState state)
	{
		this.setState(address.getCasedRawLeadSlash(), state);
	}
	
	public void setState(CellAddress address, CellAddressMapping mapping)
	{
		this.setState(address, new HistoryState(mapping));
	}
	
	public void setState(String path, HistoryState state)
	{
		path = path.startsWith("/") ? path : "/" + path;
		
		state.setIds(m_currentId, m_highestId);
		
		m_historyManager.setState(path, getTitleFromPath(path), state);
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
	
	public void pushState(CellAddress address, HistoryState state)
	{
		this.pushState(address.getCasedRawLeadSlash(), state);
	}
	
	public void pushState(String path, Point point)
	{
		this.pushState(path, new HistoryState(point));
	}
	
	public void pushState(String path, CellAddressMapping mapping)
	{
		this.pushState(path, new HistoryState(mapping));
	}
	
	public void pushState(CellAddress address, CellAddressMapping mapping)
	{
		this.pushState(address, new HistoryState(mapping));
	}
	
	public void pushState(String path, HistoryState state)
	{
		HistoryState currentState = this.getCurrentState();
		
		if( currentState != null )
		{
			if( state.isEqualTo(currentState) )
			{
				return; // Just a user experience thing...I think it's weird for the back button to just refresh a cell and not move you.
			}
		}
		
		m_highestId = 0; // reset highest id.
		this.setCurrentId(m_currentId+1);
		state.setIds(m_currentId, m_highestId);
		
		path = path.startsWith("/") ? path : "/" + path;
		m_historyManager.pushState(path, getTitleFromPath(path), state);
	}
	
	public HistoryState getCurrentState()
	{
		I_JsonObject json = m_historyManager.getCurrentState();
		
		if( json != null )
		{
			HistoryState state = new HistoryState();
			state.readJson(json, m_jsonFactory);
			
			if( m_highestId == -1 )
			{
				m_highestId = state.getHighestId();
				m_currentId = state.getId();
			}
			
			return state;
		}
		else
		{
			m_highestId = 0;
			
			return null;
		}
	}
	
	public boolean hasForward()
	{
		return m_currentId < m_highestId;
	}
	
	public boolean hasBack()
	{
		return m_currentId > 0;
	}
	
	public void go(int offset)
	{
		m_historyManager.go(offset);
	}
}
