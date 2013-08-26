package swarm.client.structs;

import swarm.client.entities.smI_BufferCellListener;

/**
 * ...
 * @author 
 */
public interface smI_CellPoolDelegate 
{
	smI_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim);
	void destroyVisualization(smI_BufferCellListener visualization);
}