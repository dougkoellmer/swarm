package swarm.shared.transaction;

import java.util.HashMap;
import java.util.logging.Logger;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smRequestPathManager
{
	private static final Logger s_logger = Logger.getLogger(smRequestPathManager.class.getName());
	
	private final HashMap<Integer, smI_RequestPath> m_intToPath = new HashMap<Integer, smI_RequestPath>();
	private final HashMap<String, smI_RequestPath> m_nameToPath = new HashMap<String, smI_RequestPath>();
	
	private final boolean m_verboseRequestPaths;
	private final smA_JsonFactory m_jsonFactory;
	
	public smRequestPathManager(smA_JsonFactory jsonFactory, boolean verboseRequestPaths)
	{
		m_jsonFactory = jsonFactory;
		m_verboseRequestPaths = verboseRequestPaths;
	}
	
	public void register(smI_RequestPath path)
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
	
	public void register(smI_RequestPath[] paths)
	{
		for( int i = 0; i < paths.length; i++ )
		{
			register(paths[i]);
		}
	}
	
	public void putToJson(smI_JsonObject json, smI_RequestPath path)
	{
		if( m_verboseRequestPaths )
		{
			m_jsonFactory.getHelper().putString(json, smE_JsonKey.requestPath, path.getName());
		}
		else
		{
			m_jsonFactory.getHelper().putInt(json, smE_JsonKey.requestPath, path.getId());
		}
	}
	
	public smI_RequestPath getFromJson(smI_JsonObject json)
	{
		Integer id = m_jsonFactory.getHelper().getInt(json, smE_JsonKey.requestPath);
		if( id != null )
		{
			return m_intToPath.get(id);
		}
		
		String name = m_jsonFactory.getHelper().getString(json, smE_JsonKey.requestPath);
		if( name != null )
		{
			return m_nameToPath.get(name);
		}
		
		return null;
	}
}
