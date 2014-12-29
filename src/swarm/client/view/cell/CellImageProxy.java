package swarm.client.view.cell;

import swarm.client.view.cell.MetaImageLoader.MetaImage;

public class CellImageProxy
{
	public static interface I_Listener
	{
		void onLoaded(CellImageProxy entry);
		void onRendered(CellImageProxy entry);
	}
	
	private static final double META_IMAGE_RENDER_DELAY__SHOULD_BE = 2.0;
	private static final double META_IMAGE_RENDER_DELAY__DEFINITELY_SHOULD_BE = META_IMAGE_RENDER_DELAY__SHOULD_BE + 0.0;
	
	protected E_ImageLoadState m_state;
	
	private double m_timeRendering = 0.0;
	
	protected I_Listener m_listener;
	
	protected CellImageProxy()
	{
		m_state = E_ImageLoadState.NOT_SET;
		m_timeRendering = 0.0;
	}
	
	public E_ImageLoadState getState()
	{
		return m_state;
	}
	
	public double getTimeRendering()
	{
		return m_state.ordinal() >= E_ImageLoadState.RENDERING.ordinal() ? m_timeRendering : 0.0;
	}
	
	void update(double timestep)
	{
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
	
	protected void onQueued()
	{
		m_state = E_ImageLoadState.QUEUED;
	}
	
	protected void onLoadFailed()
	{
		m_state = E_ImageLoadState.FAILED;
	}
	
	protected void resetRenderingState()
	{
		m_state = E_ImageLoadState.RENDERING;
		m_timeRendering = 0.0;
	}
}
