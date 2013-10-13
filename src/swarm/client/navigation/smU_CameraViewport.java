package swarm.client.navigation;

import swarm.client.managers.smCameraManager;
import swarm.shared.entities.smA_Grid;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.utils.smU_Math;

public class smU_CameraViewport
{
	private static final smPoint s_utilPoint1 = new smPoint();
	
	public static double calcViewWindowWidth(smA_Grid grid)
	{
		return grid.getCellWidth() + grid.getCellPadding()*2;
	}
	
	public static double calcViewWindowHeight(smA_Grid grid, double cellHudHeight)
	{
		if( cellHudHeight > 0 )
		{
			return grid.getCellHeight() + cellHudHeight + grid.getCellPadding()*3;
		}
		else
		{
			return grid.getCellHeight() + cellHudHeight + grid.getCellPadding()*2;
		}
	}
	
	public static void calcViewWindowCenter(smA_Grid grid, smGridCoordinate coord, double cellHudHeight, smPoint point_out)
	{		
		grid.calcCoordCenterPoint(coord, 1, point_out);
		
		if( cellHudHeight > 0 )
		{
			double offsetY = (grid.getCellPadding() + cellHudHeight)/2;
			
			point_out.incY(-offsetY);
		}
	}
	
	public static void calcViewWindowTopLeft(smA_Grid grid, smGridCoordinate coord, double cellHudHeight, smPoint point_out)
	{
		calcViewWindowCenter(grid, coord, cellHudHeight, point_out);
		point_out.incX(-calcViewWindowWidth(grid));
		point_out.incY(-calcViewWindowHeight(grid, cellHudHeight));
	}
	
	public static void calcConstrainedCameraPoint(smA_Grid grid, smGridCoordinate coord, smPoint cameraPoint, double viewWidth, double viewHeight, double cellHudHeight, smPoint point_out)
	{
		s_utilPoint1.copy(cameraPoint); // in case cameraPoint and point_out are the same reference.
		
		double minViewWidth = calcViewWindowWidth(grid);
		double minViewHeight = calcViewWindowHeight(grid, cellHudHeight);
		
		calcViewWindowCenter(grid, coord, cellHudHeight, point_out);

		if( viewWidth < minViewWidth )
		{
			double diff = (minViewWidth - viewWidth)/2;
			double x = smU_Math.clamp(s_utilPoint1.getX(), point_out.getX() - diff, point_out.getX() + diff);
			point_out.setX(x);
		}
		
		if( viewHeight < minViewHeight )
		{
			double diff = (minViewHeight - viewHeight)/2;
			double y = smU_Math.clamp(s_utilPoint1.getY(), point_out.getY() - diff, point_out.getY() + diff);
			point_out.setY(y);
		}
	}
	
	/*public static void calcConstrainedCameraPoint(smA_Grid grid, smGridCoordinate coord, smPoint cameraPoint, smPoint point_out)
	{
		smCameraManager cameraMngr = this.m_appContext.cameraMngr;
		
		double viewWidth = cameraMngr.getCamera().getViewWidth();
		double viewHeight = cameraMngr.getCamera().getViewHeight();
		
		this.calcConstrainedCameraPoint(grid, coord, cameraPoint, viewWidth, viewHeight, point_out);
	}*/
}
