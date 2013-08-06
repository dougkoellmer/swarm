package b33hive.shared.transaction;

import java.util.HashMap;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhRequestPathManager
{
	private final HashMap<Integer, bhI_RequestPath> m_intToPath = new HashMap<Integer, bhI_RequestPath>();
	private final HashMap<String, bhI_RequestPath> m_nameToPath = new HashMap<String, bhI_RequestPath>();
	
	private final boolean m_verboseRequestPaths;
	
	public bhRequestPathManager(boolean verboseRequestPaths)
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
			bh.jsonFactory.getHelper().putString(json, bhE_JsonKey.requestPath, path.getName());
		}
		else
		{
			bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.requestPath, path.getId());
		}
	}
	
	public bhI_RequestPath getFromJson(bhI_JsonObject json)
	{
		Integer id = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.requestPath);
		if( id != null )
		{
			return m_intToPath.get(id);
		}
		
		String name = bh.jsonFactory.getHelper().getString(json, bhE_JsonKey.requestPath);
		if( name != null )
		{
			return m_nameToPath.get(name);
		}
		
		return null;
	}
}
