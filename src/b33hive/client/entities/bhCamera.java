package b33hive.client.entities;

import b33hive.shared.app.bhS_App;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.structs.bhRect;
import b33hive.shared.structs.bhVector;

/**
 * ...
 * @author
 */
public class bhCamera
{
	private static final double DIRTY_VALUE = -1;
	
	private final bhPoint m_position = new bhPoint();
	private final bhVector m_velocity = new bhVector();
	
	private final bhRect m_viewRect = new bhRect();
	
	private double m_cachedDistanceRatio = DIRTY_VALUE;
	private double m_lastZ = Double.NaN;

	private double m_cachedMaxZ = DIRTY_VALUE;
	
	private static final bhCamera s_instance = new bhCamera();
	
	public bhCamera()
	{
	}
	
	public void onGridSizeChanged()
	{
		bhCamera.this.m_cachedMaxZ = DIRTY_VALUE;
	}
	
	private static double calcZFromDistanceRatio(double distanceRatio, double depthOfField)
	{
		return -depthOfField * Math.tan((Math.PI*distanceRatio) / 2 - Math.PI/2);
	}
	
	public double calcMaxZ()
	{
		return 700;
		/*
		if( m_cachedMaxZ != DIRTY_VALUE )
		{
			return m_cachedMaxZ;
		}
		
		bhClientGrid grid = bhClientGrid.getInstance();
		double maxDimension = this.calcMinViewDimension();
		double maxDistanceRatio = (maxDimension/2) / grid.calcPixelWidth();
		m_cachedMaxZ = calcZFromDistanceRatio(maxDistanceRatio, bhS_App.DEPTH_OF_FIELD);
		
		m_cachedMaxZ = m_cachedMaxZ < bhS_App.MIN_MAX_Z ? bhS_App.MIN_MAX_Z : m_cachedMaxZ;
		
		return m_cachedMaxZ;*/
	}
	
	public static bhCamera getInstance()
	{
		return s_instance;
	}

	public double calcDistanceRatio()
	{
		return this.calcDistanceRatio(0.0);
	}

	public double calcDistanceRatio(double zTarget)
	{
		if ( zTarget != 0 )
		{
			return 1 - Math.atan((m_position.getZ()-zTarget) / bhS_App.DEPTH_OF_FIELD) / (Math.PI / 2);
		}
		else
		{
			if ( m_cachedDistanceRatio == DIRTY_VALUE )
			{
				m_cachedDistanceRatio = 1 - Math.atan(m_position.getZ() / bhS_App.DEPTH_OF_FIELD) / (Math.PI / 2);
			}
		}
		
		return m_cachedDistanceRatio;
	}
	
	public void calcWorldPoint(bhPoint screenPoint, bhPoint outPoint)
	{
		double distanceRatio = this.calcDistanceRatio();
		
		outPoint.copy(screenPoint);
		outPoint.inc( -this.m_viewRect.getWidth() / 2, -this.m_viewRect.getHeight() / 2, 0);
		outPoint.scaleByNumber(1 / distanceRatio);
		outPoint.inc( this.m_position.getX(), this.m_position.getY(), 0);
	}
	
	public void calcScreenPoint(bhPoint worldPoint, bhPoint outPoint)
	{
		double distanceRatio = this.calcDistanceRatio(worldPoint.getZ());
		
		outPoint.copy(worldPoint);
		outPoint.inc( -this.m_position.getX(), -this.m_position.getY(), 0);
		outPoint.scaleByNumber(distanceRatio);
		outPoint.inc(this.m_viewRect.getWidth() / 2, this.m_viewRect.getHeight() / 2, 0);
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
	
	public bhPoint getPosition()
	{
		return m_position;
	}
	
	public bhVector getVelocity()
	{
		return m_velocity;
	}
}