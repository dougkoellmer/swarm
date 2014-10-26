package swarm.client.managers;

import java.util.ArrayList;

import swarm.client.entities.BufferCell;
import swarm.client.structs.BufferCellPool;
import swarm.server.handlers.normal.getCellAddress;

abstract class A_BufferCellList
{
	protected final BufferCellPool m_cellPool;
	protected final ArrayList<BufferCell> m_cellList = new ArrayList<BufferCell>();
	
	A_BufferCellList(BufferCellPool pool)
	{
		m_cellPool = pool;
	}
	
	void drain()
	{
		for ( int i = 0; i < this.m_cellList.size(); i++ )
		{
			BufferCell ithCell = this.m_cellList.get(i);
			
			if( ithCell != null )
			{
				m_cellPool.deallocCell(ithCell);
			}
		}
		
		this.m_cellList.clear();
	}
	
	protected static boolean swap(int m, int n, A_BufferCellList from, A_BufferCellList to, boolean checkIsLoaded)
	{
		int index = from.getCellAtAbsoluteCoord_protected(m, n);
		
		if( index < 0 )  return false;
		
		BufferCell cell = from.m_cellList.get(index);
		
		if( cell == null )  return false;
		
		if( checkIsLoaded && !cell.getVisualization().isLoaded() )  return false;
		
		from.m_cellList.set(index, null);
		to.m_cellList.add(cell);
		
		return true;
	}
	
	protected int getCellAtAbsoluteCoord_protected(int m, int n)
	{
		int index = -1;
		for( int i = 0; i < m_cellList.size(); i++ )
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell != null && ithCell.getCoordinate().isEqualTo(m, n) )
			{
				return i;
			}
		}
		
		return index;
	}
	
	public BufferCell getCellAtAbsoluteCoord(int m, int n)
	{
		int index = getCellAtAbsoluteCoord_protected(m, n);
		
		return index >= 0 ? m_cellList.get(index) : null;
	}
	
	BufferCell removeCellAtAbsCoord(int m, int n)
	{
		for( int i = 0; i < m_cellList.size(); i++ )
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell == null )  continue;
			
			if( ithCell.getCoordinate().isEqualTo(m, n) )
			{
				m_cellList.set(i, null);
				
				return ithCell;
			}
		}
		
		return null;
	}
}
