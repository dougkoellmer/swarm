package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.client.entities.I_BufferCellListener;

/**
 * ...
 * @author 
 */
public interface I_CellPoolDelegate 
{
	I_BufferCellListener createVisualization(BufferCell bufferCell, int width, int height, int padding, int subCellDim);
	void destroyVisualization(I_BufferCellListener visualization);
}