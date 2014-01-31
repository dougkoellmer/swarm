package swarm.shared.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class RequestPathManager
{
	private static final Logger s_logger = Logger.getLogger(RequestPathManager.class.getName());
	
	private final HashMap<Integer, I_RequestPath> m_intToPath = new HashMap<Integer, I_RequestPath>();
	private final HashMap<String, I_RequestPath> m_nameToPath = new HashMap<String, I_RequestPath>();
	
	private final boolean m_verboseRequestPaths;
	private final A_JsonFactory m_jsonFactory;
	
	public RequestPathManager(A_JsonFactory jsonFactory, boolean verboseRequestPaths)
	{
		m_jsonFactory = jsonFactory;
		m_verboseRequestPaths = verboseRequestPaths;
	}
	
	public Iterator<String> getPaths()
	{
		return m_nameToPath.keySet().iterator();
	}
	
	public I_RequestPath getPath(String path)
	{
		return m_nameToPath.get(path);
	}
	
	public I_RequestPath getPath(Integer id)
	{
		return m_intToPath.get(id);
	}
	
	public void register(I_RequestPath path)
	{
		if( m_intToPath.containsKey(path.getId()) )
		{
			String error = "Request path manager already contains id key: " + path.getId();
			s_logger.severe(error);
			
			throw new Error(error);
		}
		
		if( m_intToPath.containsKey(path.getName()) )
		{
			String error = "Request path manager already contains name key: " + path.getName();
			s_logger.severe(error);
			
			throw new Error(error);
		}
		
		m_intToPath.put(path.getId(), path);
		m_nameToPath.put(path.getName(), path);
	}
	
	public void register(I_RequestPath[] paths)
	{
		for( int i = 0; i < paths.length; i++ )
		{
			register(paths[i]);
		}
	}
	
	public void putToJson(I_RequestPath path, I_JsonObject json_out)
	{
		if( m_verboseRequestPaths )
		{
			m_jsonFactory.getHelper().putString(json_out, E_JsonKey.requestPath, path.getName());
		}
		else
		{
			m_jsonFactory.getHelper().putInt(json_out, E_JsonKey.requestPath, path.getId());
		}
	}
	
	public I_RequestPath getFromJson(I_JsonObject json)
	{
		Integer id = m_jsonFactory.getHelper().getInt(json, E_JsonKey.requestPath);
		if( id != null )
		{
			return m_intToPath.get(id);
		}
		
		String name = m_jsonFactory.getHelper().getString(json, E_JsonKey.requestPath);
		if( name != null )
		{
			return m_nameToPath.get(name);
		}
		
		return null;
	}
}
