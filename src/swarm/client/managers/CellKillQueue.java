package swarm.client.managers;

import java.util.ArrayList;

import swarm.client.entities.BufferCell;
import swarm.client.entities.ClientGrid;
import swarm.client.entities.ClientGrid.Obscured;
import swarm.client.structs.BufferCellPool;
import swarm.shared.utils.U_Bits;

class CellKillQueue extends A_BufferCellList
{
	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured();
	private final double m_deathCountdown;
	
	CellKillQueue(CellBufferManager parent, int subCellCount, BufferCellPool pool, double deathCountdown)
	{
		super(parent, subCellCount, pool);
		
		m_deathCountdown = deathCountdown;
	}
	
	void sentenceToDeath(BufferCell cell)
	{
		m_cellList.add(cell);
		cell.sentenceToDeath(m_deathCountdown);
	}
	
	void update(double timestep)
	{
		for( int i = m_cellList.size()-1; i >= 0; i--)
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell == null )
			{
				m_cellList.remove(i);
			}
			else
			{
				ithCell.updateDeathRowTimer(timestep);
			}
		}
	}
	
//	private boolean isEverythingLoadedAbove(BufferCell ithCell)
//	{
//		
//	}
	
	private boolean isEverythingLoadedUnderneath(BufferCell ithCell)
	{
		if( m_subCellCount <= 1 )  return true;
		
		for( int subCellCount = m_subCellCount >>> 1; subCellCount > 0; subCellCount >>>= 1 )
		{
			int index = U_Bits.calcBitPosition(subCellCount);
			CellBuffer buffer = m_parent.getDisplayBuffer(index);
			
			for( int j = 0; j < buffer.m_cellList.size(); j++ )
			{
				BufferCell jthCell = buffer.m_cellList.get(j);
				
				if( jthCell == null )  continue;

				if( !jthCell.getVisualization().isLoaded() )
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	void clearTheDead(ClientGrid grid)
	{
		for( int i = m_cellList.size()-1; i >= 0; i--)
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell == null )
			{
				m_cellList.remove(i);
			}
			else if( !ithCell.getVisualization().isLoaded() || ithCell.kill() || isEverythingLoadedUnderneath(ithCell) )
			{
				if( m_subCellCount > 1 )
				{
					for( int subCellCount = m_subCellCount >>> 1; subCellCount > 0; subCellCount >>>= 1 )
					{
						int index = U_Bits.calcBitPosition(subCellCount);
						CellBuffer buffer = m_parent.getDisplayBuffer(index);
						
						for( int j = 0; j < buffer.m_cellList.size(); j++ )
						{
							BufferCell jthCell = buffer.m_cellList.get(j);
							
							if( jthCell == null )  continue;
							
							if( grid.isObscured(jthCell.getCoordinate().getM(), jthCell.getCoordinate().getN(), subCellCount, m_subCellCount, m_obscured) )
							{
								if( m_subCellCount == m_obscured.subCellCount && ithCell.getCoordinate().isEqualTo(m_obscured.m, m_obscured.n) )
								{
									jthCell.getVisualization().onRevealed();
								}
							}
						}
					}
				}
				
				m_cellPool.deallocCell(ithCell);
				m_cellList.remove(i);
			}
		}
	}
}
