package b33hive.client.managers;

import java.util.logging.Logger;

import b33hive.client.entities.bhCamera;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.structs.bhTolerance;
import b33hive.shared.structs.bhVector;

public class bhCameraManager
{
	private static final double SNAP_TOLERANCE = .00001;
	private static final Logger s_logger = Logger.getLogger(bhCameraManager.class.getName());
	
	private final bhPoint m_cameraOrigin = new bhPoint();
	private final bhPoint m_targetPosition = new bhPoint();
	private final bhVector m_diffVector = new bhVector();
	private final bhVector m_utilVector = new bhVector();
	private final bhPoint m_utilPoint = new bhPoint();
	
	private double m_startY = 0;
	private double m_lengthToTravel = 0;
	private double m_xProgress = 0;
	private double m_exponent = 0.0;
	private double m_snapTime = 0.0;
	
	private int m_cameraAtRestFrameCount = 1;
	
	private final bhCamera m_camera;
	
	private double m_minSnapTime = 0;
	private double m_snapTimeRange = 0;
	
	public bhCameraManager(bhCamera camera, double minSnapTime, double snapTimeRange)
	{
		m_minSnapTime = minSnapTime;
		m_snapTimeRange = snapTimeRange;
		
		m_camera = camera;
	}
	
	public bhPoint getTargetPosition()
	{
		return m_targetPosition;
	}
	
	private double calcY(double x)
	{
		double y = Math.pow(x*(1/m_snapTime), m_exponent);
		
		return y;
	}
	
	protected void update(double timeStep)
	{//s_logger.severe(m_camera.getPosition() + "");
		if ( m_xProgress == 0 )
		{
			m_cameraAtRestFrameCount++;
			
			return;
		}

		m_xProgress -= timeStep;
		
		if ( m_xProgress <= 0 )
		{
			this.setCameraPosition(m_targetPosition, true);
			
			return;
		}
		
		double progressRatio = calcY(m_xProgress);
		progressRatio = 1 - progressRatio / m_startY;
		
		m_utilVector.copy(m_diffVector);
		m_utilVector.scaleByNumber(progressRatio);
		
		m_camera.getPosition().copy(m_cameraOrigin);
		m_camera.getPosition().add(m_utilVector);
		
		m_camera.update();
	}
	
	protected void setTargetPosition(bhPoint point, boolean instant)
	{
		bhPoint oldTargetPosition = m_utilPoint;
		oldTargetPosition.copy(m_targetPosition);
		double maxZ = m_camera.calcMaxZ();
		
		//--- DRK > This is here so that given target points can selectively choose which
		//---		components they want to update. For example a zoom action might set x
		//---		and y to NaN values because it only wants to update z.
		for( int i = 0; i < 3; i++ )
		{
			double sourceComponent = point.getComponent(i);
			if( !Double.isNaN(sourceComponent) )
			{
				m_targetPosition.setComponent(i, sourceComponent);
			}
		}
		
		//--- DRK < Constrain Z position.
		if( m_targetPosition.getZ() > maxZ )
		{
			m_targetPosition.setZ(maxZ);
		}
		else if( m_targetPosition.getZ() < 0 )
		{
			m_targetPosition.setZ(0);
		}
		
		if( instant )
		{
			oldTargetPosition.calcDifference(m_targetPosition, m_utilVector);
			if( m_utilVector.calcLengthSquared() < SNAP_TOLERANCE ) // kinda hacky
			{
				this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
				
				return; // let the camera continue along its trajectory, instead of recalculating it.
				// NOTE: DRK > Don't know what above comment means now...maybe just a copy/paste from another location?
			}
			
			m_cameraAtRestFrameCount = 0;
			this.setCameraPosition(m_targetPosition, false);
			
			return;
		}
		
		m_cameraOrigin.copy(m_camera.getPosition());
		m_targetPosition.calcDifference(m_cameraOrigin, m_diffVector);
		
		double lengthSquared = m_diffVector.calcLengthSquared();
		
		if ( lengthSquared < SNAP_TOLERANCE)
		{
			this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
			
			return;
		}

		m_lengthToTravel = Math.sqrt(lengthSquared);
		
		//TODO: Leaving this constant here cause it's prolly not a final solution anyway.
		//		Obviously should be moved out someplace that's a little more configurable.
		double maxDistance = 1024 * bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
		
		double distanceRatio = m_lengthToTravel / maxDistance;
		
		if( distanceRatio < 1 )
		{
			distanceRatio = Math.pow(distanceRatio, 1.0/3.0);
		}
		
		if( !instant )
		{
			double timeToTravel = m_minSnapTime + distanceRatio * m_snapTimeRange;
			
			m_snapTime = timeToTravel;
		}
		else
		{
			//m_snapTime = 0.01;
		}
		
		//timeToTravel = bhU_Math.clamp(timeToTravel, bhS_App.MIN_SNAP_TIME, bhS_App.MAX_SNAP_TIME);

		
		
		final double MIN_EXPONENT = 3;
		final double MAX_EXPONENT = 5;
		final double EXPONENT_RANGE = MAX_EXPONENT - MIN_EXPONENT;
		
		m_exponent = MIN_EXPONENT + distanceRatio * EXPONENT_RANGE;
		//m_exponent = bhU_Math.clamp(m_exponent, MIN_EXPONENT, MAX_EXPONENT);

		m_startY = calcY(m_snapTime);
		
		m_xProgress = m_snapTime;
		
		//s_logger.info(m_exponent + " " + m_snapTime + " " + m_startY);
		//s_logger.info(m_lengthToTravel + " " + distanceRatio + " distanceRatio");
		
		m_cameraAtRestFrameCount = 0;
	}
	
	//--- DRK > NOTE: Must make sure to manually update the cell buffer manager if necessary after this call.
	protected void setCameraPosition(bhPoint point, boolean enforceZConstraints)
	{
		m_targetPosition.copy(point);
		
		if( enforceZConstraints )
		{
			double maxZ = m_camera.calcMaxZ();
			
			if( m_targetPosition.getZ() > maxZ )
			{
				m_targetPosition.setZ(maxZ);
			}
			else if( m_targetPosition.getZ() < 0 )
			{
				m_targetPosition.setZ(0);
			}
		}
		
		m_camera.getPosition().copy(m_targetPosition);
		
		m_camera.update();
		
		m_cameraOrigin.copy(m_targetPosition);
		
		m_xProgress = 0;
	}
	
	public boolean didCameraJustComeToRest()
	{
		return m_cameraAtRestFrameCount == 1;
	}
	
	public boolean isCameraAtRest()
	{
		if( m_cameraAtRestFrameCount >= 1 )
		{
			bhU_Debug.ASSERT(m_targetPosition.isEqualTo(m_camera.getPosition(), bhTolerance.EXACT), "isCameraAtRest1");
			
			return true;
		}

		return false;
	}
}
