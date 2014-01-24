package swarm.client.navigation;

import java.util.logging.Logger;

import swarm.client.input.smBrowserAddressManager;
import swarm.client.input.smBrowserHistoryManager;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smPoint;

public class smHistoryStateManager
{
	private static final Logger s_logger = Logger.getLogger(smHistoryStateManager.class.getName());
	
	public static interface I_Listener
	{
		void onStateChange(String url, smHistoryState state);
	}
	
	private final smBrowserAddressManager m_addressMngr;
	private final smBrowserHistoryManager m_historyManager;
	
	private final I_Listener m_listener;
	private final String m_defaultPageTitle;
	private final smA_JsonFactory m_jsonFactory;
	
	private int m_currentId = 0;
	private int m_highestId = -1;
	
	public smHistoryStateManager(smA_JsonFactory jsonFactory, String defaultPageTitle, I_Listener listener, smBrowserAddressManager addressMngr)
	{
		m_jsonFactory = jsonFactory;
		m_defaultPageTitle = defaultPageTitle;
		m_listener = listener;
		m_addressMngr = addressMngr;
		
		m_historyManager = new smBrowserHistoryManager(jsonFactory, new smBrowserHistoryManager.I_Listener()
		{
			@Override
			public void onStateChange(String url, smI_JsonObject json)
			{
				smHistoryState state = null;
				
				if( json != null )
				{
					state = new smHistoryState();
					state.readJson(m_jsonFactory, json);
					
					int id = state.getId();
					
					//s_logger.severe("state id: " + id + ", currentId: " + m_currentId);
					
					smHistoryStateManager.this.setCurrentId(id);
					
					//--- DRK > Resave the state so accurate highestId is preserved if user navigates away from page.
					String justThePath = m_addressMngr.getCurrentPath();
					smHistoryStateManager.this.setState(justThePath, state);
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
	
	public void setState(String path, smHistoryState state)
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
		
		m_highestId = 0; // reset highest id.
		this.setCurrentId(m_currentId+1);
		state.setIds(m_currentId, m_highestId);
		
		path = path.startsWith("/") ? path : "/" + path;
		m_historyManager.pushState(path, getTitleFromPath(path), state);
	}
	
	public smHistoryState getCurrentState()
	{
		smI_JsonObject json = m_historyManager.getCurrentState();
		
		if( json != null )
		{
			smHistoryState state = new smHistoryState();
			state.readJson(m_jsonFactory, json);
			
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
