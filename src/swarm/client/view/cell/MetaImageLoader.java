package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import swarm.client.view.cell.CellImageProxy.I_Listener;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;

public class MetaImageLoader
{	
	static final class MetaImage extends CellImageProxy
	{
		private final ImageElement m_element;
		private final String m_url;
		
		MetaImage(String url)
		{
			super();
			
			m_url = url;
			m_element = Document.get().createImageElement();
			m_element.getStyle().setWidth(100, Unit.PCT);
			m_element.getStyle().setHeight(100, Unit.PCT);
		}
		
		private void startLoad()
		{
			if( m_state == E_ImageLoadState.FAILED )
			{
				m_element.setSrc(""); // not sure if this is necessary but shouldn't hurt.
			}
			
			m_state = E_ImageLoadState.LOADING;
			m_element.setSrc(m_url);
		}
		
		private void onLoadSucceeded()
		{
			resetRenderingState();
			
			if( m_listener != null )
			{
				m_listener.onLoaded(this);
			}
		}
		
		private void onAlreadyLoaded()
		{
			resetRenderingState();
			
			if( m_listener != null )
			{
				m_listener.onLoaded(this);
			}
		}
		
		@Override protected Element getElement()
		{
			return m_element;
		}
	}
	
//	private static final Storage s_localStorage = Storage.getLocalStorageIfSupported();
	
	private final ArrayList<MetaImage> m_loadQueue = new ArrayList<MetaImage>();
	
	private final HashMap<String, MetaImage> m_entryMap = new HashMap<String, MetaImage>();
	private final ArrayList<MetaImage> m_entryList = new ArrayList<MetaImage>();
	
	private final double m_queuePopRate;
	private double m_queuePopTimer;
	
	public MetaImageLoader(double queuePopRate)
	{
		m_queuePopRate = queuePopRate;
	}
	
	MetaImage preLoad(String url)
	{
		MetaImage entry = getEntry(url);
		
		if( entry != null ) 
		{
			if( entry.isAtLeastQueued() )
			{
				return entry;
			}
		}
		else
		{
			entry = newEntry(url);
		}
		
		if( entry.isAtLeastQueued() )  return entry; // just being anal
		
		if( m_loadQueue.size() == 0 )
		{
			m_queuePopTimer = 0.0;
		}

		queue(entry);
		
		return entry;
	}
	
	MetaImage load(MetaImage entry, I_Listener listener)
	{
		entry.m_listener = listener;
		
		if( !entry.isAtLeastLoading() )
		{
			load(entry);
		}
		else
		{
			entry.onAlreadyLoaded();
		}
		
		return entry;
	}
	
	private MetaImage newEntry(String url)
	{
		MetaImage entry = new MetaImage(url);
		m_entryMap.put(url, entry);
		m_entryList.add(entry);
		
		return entry;
	}
	
	private void load(MetaImage entry)
	{
		entry.startLoad();
		addImagesLoadedListener(this, entry, entry.m_element);
	}
	
	private void queue(MetaImage entry)
	{
		m_loadQueue.add(entry);
		entry.onQueued();
	}
	
	private MetaImage getEntry(String url)
	{
		return m_entryMap.get(url);
	}
	
	private native void addImagesLoadedListener(MetaImageLoader loader, MetaImage entry, Element element)
	/*-{
			var imgLoad = new $wnd.imagesLoaded( element );
			
			imgLoad.on('done', function()
			{
				loader.@swarm.client.view.cell.MetaImageLoader::onLoadSucceeded(Lswarm/client/view/cell/MetaImageLoader$MetaImage;)(entry);
			});
			
			imgLoad.on('fail', function()
			{
				loader.@swarm.client.view.cell.MetaImageLoader::onLoadFailed(Lswarm/client/view/cell/MetaImageLoader$MetaImage;)(entry);
			});
	}-*/;
	
	private void onLoadFailed(MetaImage entry)
	{
		entry.onLoadFailed();
	}
	
	private void onLoadSucceeded(MetaImage entry)
	{
		entry.onLoadSucceeded();
	}
	
	void updateQueue(double timestep)
	{
		m_queuePopTimer += timestep;
		
		if( m_queuePopTimer > m_queuePopRate )
		{
			m_queuePopTimer = 0.0;
			
			while( m_loadQueue.size() > 0 )
			{
				MetaImage entry = m_loadQueue.remove(m_loadQueue.size()-1);
				
				if( !entry.isAtLeastLoading() )
				{
					load(entry);
					
					break;
				}
			}
		}
	}
	
	void updateEntries(double timestep)
	{
		for( int i = 0; i < m_entryList.size(); i++ )
		{
			MetaImage ithEntry = m_entryList.get(i);
			
			ithEntry.update(timestep);
		}
	}
	
//	private static native boolean canCheckMozLocalAvailability()
//	/*-{
//			return false; // for now mozIsLocallyAvailable seems a little borked and/or slow.
//			return typeof $wnd.navigator.mozIsLocallyAvailable !== 'undefined';
//	}-*/;
//	
//	private static native String getAbsoluteUrl(String url)
//	/*-{
//			var loc = window.location;
//			var url = "" + loc.protocol + "//" + loc.host + url;
//			
//			return url;
//	}-*/;
//	
//	private static native String getPathFromAbsolute(String url)
//	/*-{
//			var url = url.replace(/^.*\/\/[^\/]+/, '');
//			
//			return url;
//	}-*/;
//	
//	private static native boolean isLocallyAvailable(String url)
//	/*-{
//			if( $wnd.navigator.mozIsLocallyAvailable(url, true) )
//			{
//				return true;
//			}
//	}-*/;
}
