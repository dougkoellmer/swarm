package swarm.server.data.blob;

import java.util.HashMap;

class bhBlobTemplateManager
{	
	private final HashMap<Class<? extends bhI_Blob>, bhI_Blob> m_templates = new HashMap<Class<? extends bhI_Blob>, bhI_Blob>();
	
	bhBlobTemplateManager()
	{
		
	}
	
	private synchronized bhI_Blob registerTemplate(Class<? extends bhI_Blob> T) throws bhBlobException
	{
		//--- DRK > Early out for fringe case of second thread waiting to get into this method.
		//---		By the time it does, the template is already registered and we can early out.
		if( m_templates.containsKey(T) )
		{
			return m_templates.get(T);
		}
		
		bhI_Blob template = bhU_Blob.createBlobInstance(T);
		
		m_templates.put(T, template);
		
		return template;
	}
	
	bhI_Blob getTemplate(Class<? extends bhI_Blob> T) throws bhBlobException
	{
		bhI_Blob template = m_templates.get(T);
		
		if( template == null )
		{
			template = registerTemplate(T);
		}
		
		return template;
	}
}
