package swarm.server.data.blob;

import java.util.HashMap;

class BlobTemplateManager
{	
	private final HashMap<Class<? extends I_Blob>, I_Blob> m_templates = new HashMap<Class<? extends I_Blob>, I_Blob>();
	
	BlobTemplateManager()
	{
		
	}
	
	private synchronized I_Blob registerTemplate(Class<? extends I_Blob> T) throws BlobException
	{
		//--- DRK > Early out for fringe case of second thread waiting to get into this method.
		//---		By the time it does, the template is already registered and we can early out.
		if( m_templates.containsKey(T) )
		{
			return m_templates.get(T);
		}
		
		I_Blob template = U_Blob.createBlobInstance(T);
		
		m_templates.put(T, template);
		
		return template;
	}
	
	I_Blob getTemplate(Class<? extends I_Blob> T) throws BlobException
	{
		I_Blob template = m_templates.get(T);
		
		if( template == null )
		{
			template = registerTemplate(T);
		}
		
		return template;
	}
}
