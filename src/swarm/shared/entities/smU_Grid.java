package swarm.shared.entities;

public class smU_Grid
{
	public static double calcCellScaling(double distanceRatio, int subCellDim, int cellPadding, int cellWidth)
	{
		if( subCellDim == 1 )
		{
			return distanceRatio;
		}
		else
		{
			double scalingRatio = ((double)cellWidth+cellPadding)/((double) cellWidth);
			return (distanceRatio * scalingRatio) * ((double)subCellDim);
		}
	}
}
