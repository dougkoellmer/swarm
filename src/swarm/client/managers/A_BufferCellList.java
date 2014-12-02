package swarm.client.managers;

import java.util.ArrayList;

import swarm.client.entities.BufferCell;
import swarm.client.structs.BufferCellPool;
import swarm.server.handlers.normal.getCellAddress;

abstract class A_BufferCellList
{
	protected final int m_subCellCount;
	protected final BufferCellPool m_cellPool;
	protected final ArrayList<BufferCell> m_cellList = new ArrayList<BufferCell>();
	protected final CellBufferManager m_parent;
	
	A_BufferCellList(CellBufferManager parent, int subCellCount, BufferCellPool pool)
	{
		m_cellPool = pool;
		m_subCellCount = subCellCount;
		m_parent = parent;
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
	
	protected static BufferCell swap(int m, int n, A_BufferCellList from, A_BufferCellList to, boolean onlySwapIfLoaded)
	{
		int index = from.getCellAtAbsoluteCoord_protected(m, n);
		
		if( index < 0 )  return null;
		
		return swap(index, from, to, onlySwapIfLoaded);
	}
	
	protected static BufferCell swap(int index, A_BufferCellList from, A_BufferCellList to, boolean onlySwapIfLoaded)
	{
		BufferCell cell = from.m_cellList.get(index);
		
		if( cell == null )  return null;
		
		if( onlySwapIfLoaded && !cell.getVisualization().isLoaded() )  return null;
		
		from.m_cellList.set(index, null);
		to.m_cellList.add(cell);
		
		return cell;
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
