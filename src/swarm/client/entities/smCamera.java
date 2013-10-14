package swarm.client.entities;

import swarm.shared.app.smS_App;
import swarm.shared.structs.smPoint;
import swarm.shared.structs.smRect;
import swarm.shared.structs.smVector;

/**
 * ...
 * @author
 */
public class smCamera
{
	private static final double DIRTY_VALUE = -1;
	
	private final smPoint m_position = new smPoint();
	private final smVector m_velocity = new smVector();
	
	private final smRect m_viewRect = new smRect();
	
	private double m_cachedDistanceRatio = DIRTY_VALUE;
	private double m_lastZ = Double.NaN;

	private double m_cachedMaxZ = DIRTY_VALUE;
	
	public smCamera()
	{
	}
	
	public void onGridSizeChanged()
	{
		smCamera.this.m_cachedMaxZ = DIRTY_VALUE;
	}
	
	private static double calcZFromDistanceRatio(double distanceRatio, double depthOfField)
	{
		return -depthOfField * Math.tan((Math.PI*distanceRatio) / 2 - Math.PI/2);
	}
	
	public double calcMaxZ()
	{
		return 5000;
		/*
		if( m_cachedMaxZ != DIRTY_VALUE )
		{
			return m_cachedMaxZ;
		}
		
		smClientGrid grid = smClientGrid.getInstance();
		double maxDimension = this.calcMinViewDimension();
		double maxDistanceRatio = (maxDimension/2) / grid.calcPixelWidth();
		m_cachedMaxZ = calcZFromDistanceRatio(maxDistanceRatio, smS_App.DEPTH_OF_FIELD);
		
		m_cachedMaxZ = m_cachedMaxZ < smS_App.MIN_MAX_Z ? smS_App.MIN_MAX_Z : m_cachedMaxZ;
		
		return m_cachedMaxZ;*/
	}

	public double calcDistanceRatio()
	{
		return this.calcDistanceRatio(0.0);
	}

	public double calcDistanceRatio(double zTarget)
	{
		if ( zTarget != 0 )
		{
			return 1 - Math.atan((m_position.getZ()-zTarget) / smS_App.DEPTH_OF_FIELD) / (Math.PI / 2);
		}
		else
		{
			if ( m_cachedDistanceRatio == DIRTY_VALUE )
			{
				m_cachedDistanceRatio = 1 - Math.atan(m_position.getZ() / smS_App.DEPTH_OF_FIELD) / (Math.PI / 2);
			}
		}
		
		return m_cachedDistanceRatio;
	}
	
	public void calcWorldPoint(smPoint screenPoint, smPoint point_out)
	{
		double distanceRatio = this.calcDistanceRatio();
		
		point_out.copy(screenPoint);
		point_out.inc( -this.m_viewRect.getWidth() / 2, -this.m_viewRect.getHeight() / 2, 0);
		point_out.scaleByNumber(1 / distanceRatio);
		point_out.inc( this.m_position.getX(), this.m_position.getY(), 0);
	}
	
	public void calcScreenPoint(smPoint worldPoint, smPoint point_out)
	{
		double distanceRatio = this.calcDistanceRatio(worldPoint.getZ());
		
		point_out.copy(worldPoint);
		point_out.inc( -this.m_position.getX(), -this.m_position.getY(), 0);
		point_out.scaleByNumber(distanceRatio);
		point_out.inc(this.m_viewRect.getWidth() / 2, this.m_viewRect.getHeight() / 2, 0);
	}
	
	public void update()
	{
		if ( m_lastZ != m_position.getZ() )
		{
			m_cachedDistanceRatio = DIRTY_VALUE;
		}
		
		m_lastZ = m_position.getZ();
	}
	
	public double getViewWidth()
	{
		return m_viewRect.getWidth();
	}
	
	public double getViewHeight()
	{
		return m_viewRect.getHeight();
	}
	
	public double calcMaxViewDimension()
	{
		return Math.max(m_viewRect.getWidth(), m_viewRect.getHeight());
	}
	
	public double calcMinViewDimension()
	{
		return Math.min(m_viewRect.getWidth(), m_viewRect.getHeight());
	}
	
	public void setViewRect(double width, double height)
	{
		m_viewRect.set(width, height);
		
		m_cachedMaxZ = DIRTY_VALUE;
	}
	
	public smPoint getPosition()
	{
		return m_position;
	}
	
	public smVector getVelocity()
	{
		return m_velocity;
	}
}