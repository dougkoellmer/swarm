package b33hive.shared.transaction;

import java.util.HashMap;
import java.util.logging.Logger;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhRequestPathManager
{
	private static final Logger s_logger = Logger.getLogger(bhRequestPathManager.class.getName());
	
	private final HashMap<Integer, bhI_RequestPath> m_intToPath = new HashMap<Integer, bhI_RequestPath>();
	private final HashMap<String, bhI_RequestPath> m_nameToPath = new HashMap<String, bhI_RequestPath>();
	
	private final boolean m_verboseRequestPaths;
	private final bhA_JsonFactory m_jsonFactory;
	
	public bhRequestPathManager(bhA_JsonFactory jsonFactory, boolean verboseRequestPaths)
	{
		m_jsonFactory = jsonFactory;
		m_verboseRequestPaths = verboseRequestPaths;
	}
	
	public void register(bhI_RequestPath path)
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
	
	public void register(bhI_RequestPath[] paths)
	{
		for( int i = 0; i < paths.length; i++ )
		{
			register(paths[i]);
		}
	}
	
	public void putToJson(bhI_JsonObject json, bhI_RequestPath path)
	{
		if( m_verboseRequestPaths )
		{
			m_jsonFactory.getHelper().putString(json, bhE_JsonKey.requestPath, path.getName());
		}
		else
		{
			m_jsonFactory.getHelper().putInt(json, bhE_JsonKey.requestPath, path.getId());
		}
	}
	
	public bhI_RequestPath getFromJson(bhI_JsonObject json)
	{
		Integer id = m_jsonFactory.getHelper().getInt(json, bhE_JsonKey.requestPath);
		if( id != null )
		{
			return m_intToPath.get(id);
		}
		
		String name = m_jsonFactory.getHelper().getString(json, bhE_JsonKey.requestPath);
		if( name != null )
		{
			return m_nameToPath.get(name);
		}
		
		return null;
	}
}
