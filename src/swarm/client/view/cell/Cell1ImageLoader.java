package swarm.client.view.cell;


import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.dom.client.Element;

import swarm.client.view.cell.CellImageProxy.I_Listener;
import swarm.shared.structs.GridCoordinate;

public class Cell1ImageLoader
{
	static class Cell1Proxy extends CellImageProxy
	{
		private I_Listener m_listener;
		private Element m_element;
		
		private final int m_hash;
		
		private Cell1Proxy(int hash)
		{
			super();
			
			m_hash = hash;
		}
		
		private void onAlreadyLoaded(I_Listener listener)
		{
			m_listener = listener;
			
			resetRenderingState();
			
			if( m_listener != null )
			{
				m_listener.onLoaded(this);
			}
		}
		
		private void startLoad(Element element, I_Listener listener)
		{
			m_listener = listener;
			m_element = element;
			
			addWaitForImagesListener(element, this);
		}
		
		private native void addWaitForImagesListener(Element element, Cell1Proxy proxy)
		/*-{
				$element = $wnd.$(element);
				$element.waitForImages(function()
				{
					proxy.@swarm.client.view.cell.Cell1ImageLoader.Cell1Proxy::onLoadSucceeded(Lcom/google/gwt/dom/client/Element;)(element);
					
				}, null, true);
		}-*/;
		
		private void onLoadSucceeded(Element element)
		{
			resetRenderingState();
			
			if( m_listener != null )
			{
				m_listener.onLoaded(this);
			}
		}
		
		private void updateListener(I_Listener listener)
		{
			m_listener = listener;
		}
	}
	
	private final HashMap<Integer, Cell1Proxy> m_map = new HashMap<Integer, Cell1Proxy>();
	private final ArrayList<Cell1Proxy> m_list = new ArrayList<Cell1Proxy>();
	
	Cell1Proxy getProxy(int m, int n)
	{
		final int coordHash = GridCoordinate.hashCode(m, n);
		Cell1Proxy proxy = m_map.get(coordHash);
		
		if( proxy == null )
		{
			proxy = newCellProxy(coordHash);
		}
		
		return proxy;
	}
	
	void load(Cell1Proxy proxy, Element element, I_Listener listener)
	{
		if( proxy.isLoaded() )
		{
			proxy.onAlreadyLoaded(listener);
		}
		else
		{
			if( !proxy.isAtLeastLoading() )
			{
				proxy.startLoad(element, listener);
			}
			else
			{
				proxy.updateListener(listener);
			}
		}
	}
	
	private Cell1Proxy newCellProxy(int hash)
	{
		Cell1Proxy proxy = new Cell1Proxy(hash);
		m_map.put(hash, proxy);
		m_list.add(proxy);
		
		return proxy;
	}
	
	void updateEntries(double timestep)
	{
		for( int i = 0; i < m_list.size(); i++ )
		{
			Cell1Proxy ithEntry = m_list.get(i);
			
			ithEntry.update(timestep);
		}
	}
}
