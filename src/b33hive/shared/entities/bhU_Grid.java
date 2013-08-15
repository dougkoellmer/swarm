package b33hive.shared.entities;

public class bhU_Grid
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
