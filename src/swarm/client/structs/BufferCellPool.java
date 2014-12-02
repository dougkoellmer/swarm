package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.client.view.cell.VisualCell;
import swarm.shared.entities.A_Grid;
import swarm.shared.memory.ObjectPool;
import swarm.shared.reflection.I_Class;

/**
 * ...
 * @author 
 */
public class BufferCellPool
{	
	private final I_Class<BufferCell> m_bufferCellClass = new I_Class<BufferCell>()
	{
		@Override
		public BufferCell newInstance()
		{
			return new BufferCell();
		}
	};
	
	private I_CellPoolDelegate m_delegate = null;
	
	private final ObjectPool<BufferCell> m_pool = new ObjectPool<BufferCell>(m_bufferCellClass);
	
	public BufferCellPool() 
	{
	}
	
	public void setDelegate(I_CellPoolDelegate delegate)
	{
		m_delegate = delegate;
	}
	
	public I_CellPoolDelegate getDelegate()
	{
		return m_delegate;
	}
	
	public int getAllocCount()
	{
		return m_pool.getAllocCount();
	}
	
	public int getCheckOutCount()
	{
		return m_pool.getCheckOutCount();
	}
	
	public BufferCell allocCell(A_Grid grid, int subCellDimension, int highestPossibleSubCellCount, boolean createVisualization, int m, int n, boolean justRemovedMetaCountOverride)
	{
		BufferCell cell = m_pool.allocate();
		cell.getCoordinate().set(m, n);
		
		cell.init(grid);
		
		if( createVisualization )
		{
			cell.setVisualization
			(
				m_delegate.createVisualization(cell, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellDimension, highestPossibleSubCellCount, justRemovedMetaCountOverride)
			);
		}
		else
		{
			cell.setVisualization(null); // just to be sure
		}
		
		return cell;
	}
	
	public void deallocCell(BufferCell cell)
	{
		if( cell.getVisualization() != null )
		{
			m_delegate.destroyVisualization(cell.getVisualization());
		}
		
		cell.onCellDestroyed(); // allow gc to do it's thing

		m_pool.deallocate(cell);
	}
}