package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;

public class MetaImageLoader
{
	public static interface I_Listener
	{
		void onLoaded(Entry entry);
		void onRendered(Entry entry);
	}
	
	private static final double META_IMAGE_RENDER_DELAY__SHOULD_BE = 2.0;
	private static final double META_IMAGE_RENDER_DELAY__DEFINITELY_SHOULD_BE = 0.0;
	
	static final class Entry
	{
		private final ImageElement m_element;
		private final String m_url;
		private E_ImageLoadState m_state;
		
		private double m_timeRendering = 0.0;
		
		private boolean m_visible = false;
		private I_Listener m_listener;
		
		Entry(String url)
		{
			m_url = url;
			m_element = Document.get().createImageElement();
			m_element.getStyle().setWidth(100, Unit.PCT);
			m_element.getStyle().setHeight(100, Unit.PCT);
			
			m_state = E_ImageLoadState.NOT_SET;
		}
		
		public Element getElement()
		{
			return m_element;
		}
		
		public E_ImageLoadState getState()
		{
			return m_state;
		}
		
		public double getTimeRendering()
		{
			return m_timeRendering;
		}
		
		private void update(double timestep)
		{
			if( !m_visible )  return;
			if( m_state.ordinal() < E_ImageLoadState.RENDERING.ordinal() )  return;
			
			m_timeRendering += timestep;
			
			E_ImageLoadState oldState = m_state;
			
			if( m_timeRendering >= META_IMAGE_RENDER_DELAY__DEFINITELY_SHOULD_BE )
			{
				m_state = E_ImageLoadState.DEFINITELY_SHOULD_BE_RENDERED_BY_NOW;
			}
			else if( m_timeRendering >= META_IMAGE_RENDER_DELAY__SHOULD_BE )
			{
				m_state = E_ImageLoadState.SHOULD_BE_RENDERED_BY_NOW;
			}
			
			if( m_listener != null )
			{
				if( oldState.ordinal() <= E_ImageLoadState.RENDERING.ordinal() && m_state.ordinal() > E_ImageLoadState.RENDERING.ordinal() )
				{
					m_listener.onRendered(this);
				}
			}
		}
		
		boolean isAtLeastQueued()
		{
			return this.m_state.ordinal() >= E_ImageLoadState.QUEUED.ordinal();
		}
		
		boolean isAtLeastLoading()
		{
			return this.m_state.ordinal() >= E_ImageLoadState.LOADING.ordinal();
		}
		
		boolean isLoaded()
		{
			return this.m_state.ordinal() >= E_ImageLoadState.RENDERING.ordinal();
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
		
		private void onQueued()
		{
			m_state = E_ImageLoadState.QUEUED;
		}
		
		private void onLoadFailed()
		{
			m_state = E_ImageLoadState.FAILED;
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
		
		private void resetRenderingState()
		{
			m_state = E_ImageLoadState.RENDERING;
			m_timeRendering = 0.0;
		}
		
		boolean isAttached()
		{
			return m_visible;
		}
		
		void onAttached()
		{
			m_visible = true;
			
			if( m_state.ordinal() >= E_ImageLoadState.RENDERING.ordinal() )
			{
				resetRenderingState();
			}
		}
		
		void onDettached()
		{
			m_visible = false;
			m_listener = null;
			
			if( m_element.getParentNode() != null )
			{
				m_element.removeFromParent();
			}
		}
	}
	
//	private static final Storage s_localStorage = Storage.getLocalStorageIfSupported();
	
	private final ArrayList<Entry> m_loadQueue = new ArrayList<Entry>();
	
	private final HashMap<String, Entry> m_entryMap = new HashMap<String, Entry>();
	private final ArrayList<Entry> m_entryList = new ArrayList<Entry>();
	
	private final double m_queuePopRate;
	private double m_queuePopTimer;
	
	public MetaImageLoader(double queuePopRate)
	{
		m_queuePopRate = queuePopRate;
	}
	
	Entry preLoad(String url)
	{
		Entry entry = getEntry(url);
		
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
	
	Entry load(Entry entry, I_Listener listener)
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
	
	private Entry newEntry(String url)
	{
		Entry entry = new Entry(url);
		m_entryMap.put(url, entry);
		m_entryList.add(entry);
		
		return entry;
	}
	
	private void load(Entry entry)
	{
		entry.startLoad();
		addImagesLoadedListener(this, entry, entry.m_element);
	}
	
	private void queue(Entry entry)
	{
		m_loadQueue.add(entry);
		entry.onQueued();
	}
	
	private Entry getEntry(String url)
	{
		return m_entryMap.get(url);
	}
	
	private native void addImagesLoadedListener(MetaImageLoader loader, Entry entry, Element element)
	/*-{
			var imgLoad = new $wnd.imagesLoaded( element );
			
			imgLoad.on('done', function()
			{
				loader.@swarm.client.view.cell.MetaImageLoader::onLoadSucceeded(Lswarm/client/view/cell/MetaImageLoader$Entry;)(entry);
			});
			
			imgLoad.on('fail', function()
			{
				loader.@swarm.client.view.cell.MetaImageLoader::onLoadFailed(Lswarm/client/view/cell/MetaImageLoader$Entry;)(entry);
			});
	}-*/;
	
	private void onLoadFailed(Entry entry)
	{
		entry.onLoadFailed();
	}
	
	private void onLoadSucceeded(Entry entry)
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
				Entry entry = m_loadQueue.remove(m_loadQueue.size()-1);
				
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
			Entry ithEntry = m_entryList.get(i);
			
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
