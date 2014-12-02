package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.client.entities.I_BufferCellVisualization;

/**
 * ...
 * @author 
 */
public interface I_CellPoolDelegate 
{
	I_BufferCellVisualization createVisualization(BufferCell bufferCell, int width, int height, int padding, int subCellDim, int highestPossibleSubCellCount, boolean justRemovedMetaCountOverride);
	void destroyVisualization(I_BufferCellVisualization visualization);
}