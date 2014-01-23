package swarm.client.entities;

import swarm.client.managers.smGridManager;
import swarm.shared.app.smS_App;
import swarm.shared.entities.smA_Grid;
import swarm.shared.structs.smPoint;
import swarm.shared.structs.smRect;
import swarm.shared.structs.smVector;

/**
 * ...
 * @author
 */
public class smCamera
{
	public interface I_MaxZAlgorithm
	{
		public double calcMaxZ();
	}
	
	public static class DefaultMaxZAlgorithm implements I_MaxZAlgorithm
	{
		private smCamera m_camera;
		private smA_Grid m_grid;
		
		public DefaultMaxZAlgorithm()
		{
			
		}
		
		public void init(smA_Grid grid, smCamera camera)
		{
			m_camera = camera;
			m_grid = grid;
		}
		
		@Override
		public double calcMaxZ()
		{
			double minViewDimension = m_camera.calcMinViewDimension();
			double maxGridSize = Math.max(m_grid.calcPixelWidth(), m_grid.calcPixelHeight());
			double maxDistanceRatio = (minViewDimension/2) / maxGridSize;
			
			double maxZ = calcZFromDistanceRatio(maxDistanceRatio, smS_App.DEPTH_OF_FIELD);
			
			maxZ = maxZ < smS_App.MIN_MAX_Z ? smS_App.MIN_MAX_Z : maxZ;
			
			return maxZ;
		}
	}
	
	private static final double DIRTY_VALUE = -1;
	
	private final smPoint m_position = new smPoint();
	private final smVector m_velocity = new smVector();
	
	private final smRect m_viewRect = new smRect();
	
	private double m_cachedDistanceRatio = DIRTY_VALUE;
	private double m_lastZ = Double.NaN;

	private double m_cachedMaxZ = DIRTY_VALUE;
	
	private final I_MaxZAlgorithm m_maxZAlgorithm;
	
	public smCamera(I_MaxZAlgorithm maxZAlgorithm)
	{
		m_maxZAlgorithm = maxZAlgorithm;
	}
	
	public void onGridSizeChanged()
	{
		smCamera.this.m_cachedMaxZ = DIRTY_VALUE;
	}
	
	public static double calcZFromDistanceRatio(double distanceRatio, double depthOfField)
	{
		return -depthOfField * Math.tan((Math.PI*distanceRatio) / 2 - Math.PI/2);
	}
	
	public double calcMaxZ()
	{
		if( m_cachedMaxZ != DIRTY_VALUE )
		{
			return m_cachedMaxZ;
		}
		m_cachedMaxZ = this.m_maxZAlgorithm.calcMaxZ();
		
		return m_cachedMaxZ;
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