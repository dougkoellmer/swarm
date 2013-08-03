package b33hive.shared.transaction;

import java.util.HashMap;

import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhRequestPathManager
{
	private static bhRequestPathManager s_instance = null;
	
	private final HashMap<Integer, bhI_RequestPath> m_intToPath = new HashMap<Integer, bhI_RequestPath>();
	private final HashMap<String, bhI_RequestPath> m_nameToPath = new HashMap<String, bhI_RequestPath>();
	
	private final boolean m_verboseRequestPaths;
	
	public static void startUp(boolean verboseRequestPaths)
	{
		s_instance = new bhRequestPathManager(verboseRequestPaths);
	}
	
	public static bhRequestPathManager getInstance()
	{
		return s_instance;
	}
	
	bhRequestPathManager(boolean verboseRequestPaths)
	{
		m_verboseRequestPaths = verboseRequestPaths;
	}
	
	public void register(bhI_RequestPath path)
	{
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
			bhJsonHelper.getInstance().putString(json, bhE_JsonKey.requestPath, path.getName());
		}
		else
		{
			bhJsonHelper.getInstance().putInt(json, bhE_JsonKey.requestPath, path.getId());
		}
	}
	
	public bhI_RequestPath getFromJson(bhI_JsonObject json)
	{
		Integer id = bhJsonHelper.getInstance().getInt(json, bhE_JsonKey.requestPath);
		if( id != null )
		{
			return m_intToPath.get(id);
		}
		
		String name = bhJsonHelper.getInstance().getString(json, bhE_JsonKey.requestPath);
		if( name != null )
		{
			return m_nameToPath.get(name);
		}
		
		return null;
	}
}
