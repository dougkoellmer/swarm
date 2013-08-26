package swarm.server.data.blob;

import java.util.HashMap;

class smBlobTemplateManager
{	
	private final HashMap<Class<? extends smI_Blob>, smI_Blob> m_templates = new HashMap<Class<? extends smI_Blob>, smI_Blob>();
	
	smBlobTemplateManager()
	{
		
	}
	
	private synchronized smI_Blob registerTemplate(Class<? extends smI_Blob> T) throws smBlobException
	{
		//--- DRK > Early out for fringe case of second thread waiting to get into this method.
		//---		By the time it does, the template is already registered and we can early out.
		if( m_templates.containsKey(T) )
		{
			return m_templates.get(T);
		}
		
		smI_Blob template = smU_Blob.createBlobInstance(T);
		
		m_templates.put(T, template);
		
		return template;
	}
	
	smI_Blob getTemplate(Class<? extends smI_Blob> T) throws smBlobException
	{
		smI_Blob template = m_templates.get(T);
		
		if( template == null )
		{
			template = registerTemplate(T);
		}
		
		return template;
	}
}
