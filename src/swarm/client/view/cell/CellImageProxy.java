package swarm.client.view.cell;

import com.google.gwt.dom.client.Element;

import swarm.client.view.cell.MetaImageLoader.MetaImageProxy;

public abstract class CellImageProxy
{
	protected static final double NO_LOAD_TIMEOUT = -1.0;
	
	public static interface I_Listener
	{
		void onLoaded(CellImageProxy entry);
		void onRendered(CellImageProxy entry);
		void onLoadFailed(CellImageProxy entry);
	}
	
	private static final double META_IMAGE_RENDER_DELAY__SHOULD_BE = 2.0;
	private static final double META_IMAGE_RENDER_DELAY__DEFINITELY_SHOULD_BE = META_IMAGE_RENDER_DELAY__SHOULD_BE + 0.0;
	
	protected E_ImageLoadState m_state;
	private double m_timer = 0.0;
	private boolean m_attached = false;
	protected I_Listener m_listener;
	
	protected CellImageProxy()
	{
		m_state = E_ImageLoadState.NOT_SET;
		m_timer = 0.0;
		m_attached = false;
	}
	
	protected abstract Element getElement();
	
	public E_ImageLoadState getState()
	{
		return m_state;
	}
	
	boolean isAttached()
	{
		return m_attached;
	}
	
	public double getTimeRendering()
	{
		return m_state.ordinal() >= E_ImageLoadState.RENDERING.ordinal() ? m_timer : 0.0;
	}
	
	void update(double timestep, double loadTimeout)
	{
		if( !m_attached )  return;
		
		m_timer += timestep;
		
		if( m_state == E_ImageLoadState.LOADING )
		{
			if( loadTimeout != NO_LOAD_TIMEOUT && m_timer >= loadTimeout )
			{
				m_timer = 0.0;
				onLoadFailed();
			}
			return;
		}
		else if( m_state.ordinal() < E_ImageLoadState.RENDERING.ordinal() )
		{
			  return;
		}
		
		E_ImageLoadState oldState = m_state;
		
		if( m_timer >= META_IMAGE_RENDER_DELAY__DEFINITELY_SHOULD_BE )
		{
			m_state = E_ImageLoadState.DEFINITELY_SHOULD_BE_RENDERED_BY_NOW;
		}
		else if( m_timer >= META_IMAGE_RENDER_DELAY__SHOULD_BE )
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
	
	protected void onQueued()
	{
		m_state = E_ImageLoadState.QUEUED;
	}
	
	protected abstract void onLoadFailed();
	
	protected void resetRenderingState()
	{
		m_state = E_ImageLoadState.RENDERING;
		m_timer = 0.0;
	}
	
	void onAttached()
	{
		m_attached = true;
		
		if( m_state.ordinal() >= E_ImageLoadState.RENDERING.ordinal() )
		{
			resetRenderingState();
		}
		else if( m_state == E_ImageLoadState.LOADING )
		{
			m_timer = 0.0;
		}
	}
	
	void onDettached()
	{
		m_attached = false;
		m_listener = null;
		
		Element element = getElement();
		
		if( element != null && element.getParentNode() != null )
		{
			element.removeFromParent();
		}
	}
}
