package b33hive.client.structs;

import b33hive.client.entities.bhI_BufferCellListener;

/**
 * ...
 * @author 
 */
public interface bhI_CellPoolDelegate 
{
	bhI_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim);
	void destroyVisualization(bhI_BufferCellListener visualization);
}