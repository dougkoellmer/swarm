package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.gwt.dom.client.Element;
import com.google.gwt.storage.client.Storage;

public class ImageLoader
{
	public static interface I_Listener
	{
		void onLoad();
		void onError();
	}
	
	
	
	private E_ImageLoadState m_state = E_ImageLoadState.NOT_SET;
	private final I_Listener m_listener;
	private Element m_element;
	
	
	
	public ImageLoader(I_Listener listener)
	{
		m_listener = listener;
		
		clean();
	}
	
	E_ImageLoadState getState()
	{
		return m_state;
	}
	
	
	
	void lodaMultipleImages(Element element)
	{
		
	}
	
	
	
	
	
	
	
	void clean()
	{
		m_state = E_State.NOT_SET;
		m_element = null;
	}
}
