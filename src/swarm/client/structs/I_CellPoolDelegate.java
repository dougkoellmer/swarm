package swarm.client.structs;

import swarm.client.entities.I_BufferCellListener;

/**
 * ...
 * @author 
 */
public interface I_CellPoolDelegate 
{
	I_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim);
	void destroyVisualization(I_BufferCellListener visualization);
}