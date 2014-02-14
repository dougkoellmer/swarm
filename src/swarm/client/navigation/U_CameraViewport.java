package swarm.client.navigation;

import swarm.client.entities.BufferCell;
import swarm.client.managers.CameraManager;
import swarm.client.view.cell.VisualCell;
import swarm.shared.entities.A_Grid;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.utils.U_Math;

public class U_CameraViewport
{
	private static final Point s_utilPoint1 = new Point();
	
	public static boolean isPointInViewport(A_Grid grid, GridCoordinate coord, Point point, double cellHudHeight, double extraPadding)
	{
		U_CameraViewport.calcViewWindowCenter(grid, coord, cellHudHeight, s_utilPoint1);
		double spaceX = calcCellWidthRequirement(grid)/2 + extraPadding;
		double spaceY = calcCellHeightRequirement(grid, cellHudHeight)/2 + extraPadding;

		return
			U_Math.isWithin(point.getX(), s_utilPoint1.getX()-spaceX, s_utilPoint1.getX() + spaceX) &&
			U_Math.isWithin(point.getY(), s_utilPoint1.getY()-spaceY, s_utilPoint1.getY() + spaceY) ;
	}
	
	public static double getViewPadding(A_Grid grid)
	{
		return grid.getCellPadding();
	}
	
	public static double calcCellWidthRequirement(A_Grid grid)
	{
		return grid.getCellWidth() + getViewPadding(grid)*2;
	}
	
	public static double calcCellHeightRequirement(A_Grid grid, double cellHudHeight)
	{
		if( cellHudHeight > 0 )
		{
			return grid.getCellHeight() + cellHudHeight + getViewPadding(grid)*3;
		}
		else
		{
			return grid.getCellHeight() + cellHudHeight + getViewPadding(grid)*2;
		}
	}
	
	public static void calcViewWindowCenter(A_Grid grid, GridCoordinate coord, double cellHudHeight, Point point_out)
	{		
		grid.calcCoordCenterPoint(coord, 1, point_out);
		
		if( cellHudHeight > 0 )
		{
			double offsetY = (getViewPadding(grid) + cellHudHeight)/2;
			
			point_out.incY(-offsetY);
		}
	}
	
	public static void calcViewWindowTopLeft(A_Grid grid, GridCoordinate coord, double cellHudHeight, Point point_out)
	{
		calcViewWindowCenter(grid, coord, cellHudHeight, point_out);
		point_out.incX(-calcCellWidthRequirement(grid));
		point_out.incY(-calcCellHeightRequirement(grid, cellHudHeight));
	}
	
	public static void calcConstrainedCameraPoint(A_Grid grid, GridCoordinate coord, Point cameraPoint, double viewWidth, double viewHeight, double cellHudHeight, Point point_out)
	{
		s_utilPoint1.copy(cameraPoint); // in case cameraPoint and point_out are the same reference.
		
		double minViewWidth = calcCellWidthRequirement(grid);
		double minViewHeight = calcCellHeightRequirement(grid, cellHudHeight);
		
		calcViewWindowCenter(grid, coord, cellHudHeight, point_out);

		if( viewWidth < minViewWidth )
		{
			double diff = (minViewWidth - viewWidth)/2;
			double x = U_Math.clamp(s_utilPoint1.getX(), point_out.getX() - diff, point_out.getX() + diff);
			point_out.setX(x);
		}
		
		if( viewHeight < minViewHeight )
		{
			double diff = (minViewHeight - viewHeight)/2;
			double y = U_Math.clamp(s_utilPoint1.getY(), point_out.getY() - diff, point_out.getY() + diff);
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
