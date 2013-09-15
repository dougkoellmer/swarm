package swarm.client.structs;

import swarm.client.entities.smBufferCell;
import swarm.client.view.cell.smVisualCell;
import swarm.shared.entities.smA_Grid;
import swarm.shared.memory.smObjectPool;
import swarm.shared.reflection.smI_Class;

/**
 * ...
 * @author 
 */
public class smCellPool
{	
	private final smI_Class<smBufferCell> m_bufferCellClass = new smI_Class<smBufferCell>()
	{
		@Override
		public smBufferCell newInstance()
		{
			return new smBufferCell();
		}
	};
	
	private smI_CellPoolDelegate m_delegate = null;
	
	private final smObjectPool<smBufferCell> m_pool = new smObjectPool<smBufferCell>(m_bufferCellClass);
	
	public smCellPool() 
	{
	}
	
	public void setDelegate(smI_CellPoolDelegate delegate)
	{
		m_delegate = delegate;
	}
	
	public smI_CellPoolDelegate getDelegate()
	{
		return m_delegate;
	}
	
	public int getAllocCount()
	{
		return m_pool.getAllocCount();
	}
	
	public smBufferCell allocCell(smA_Grid grid, int subCellDimension, boolean createVisualization)
	{
		smBufferCell cell = m_pool.allocate();
		
		if( createVisualization )
		{
			cell.setVisualization
			(
				m_delegate.createVisualization(grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellDimension)
			);
		}
		else
		{
			cell.setVisualization(null); // just to be sure
		}
		
		cell.init(grid);
		
		return cell;
	}
	
	public void deallocCell(smBufferCell cell)
	{
		if( cell.getVisualization() != null )
		{
			m_delegate.destroyVisualization(cell.getVisualization());
		}
		
		cell.onCellDestroyed(); // allow gc to do it's thing

		m_pool.deallocate(cell);
	}
}