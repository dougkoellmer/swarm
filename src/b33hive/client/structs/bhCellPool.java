package b33hive.client.structs;

import b33hive.client.entities.bhBufferCell;
import b33hive.client.ui.cell.bhVisualCell;
import b33hive.shared.memory.bhObjectPool;
import b33hive.shared.reflection.bhI_Class;

/**
 * ...
 * @author 
 */
public class bhCellPool
{
	private static final bhCellPool s_instance = new bhCellPool();
	
	private final bhI_Class<bhBufferCell> m_bufferCellClass = new bhI_Class<bhBufferCell>()
	{
		@Override
		public bhBufferCell newInstance()
		{
			return new bhBufferCell();
		}
	};
	
	private bhI_CellPoolDelegate m_delegate = null;
	
	private final bhObjectPool<bhBufferCell> m_pool = new bhObjectPool<bhBufferCell>(m_bufferCellClass);
	
	private bhCellPool() 
	{
	}
	
	public void setDelegate(bhI_CellPoolDelegate delegate)
	{
		m_delegate = delegate;
	}
	
	public bhI_CellPoolDelegate getDelegate()
	{
		return m_delegate;
	}
	
	public static bhCellPool getInstance()
	{
		return s_instance;
	}
	
	public int getAllocCount()
	{
		return m_pool.getAllocCount();
	}
	
	public bhBufferCell allocCell(int cellSize, boolean createVisualization)
	{
		bhBufferCell cell = m_pool.allocate();
		
		if( createVisualization )
		{
			cell.setVisualization(m_delegate.createVisualization(cellSize));
		}
		else
		{
			cell.setVisualization(null); // just to be sure
		}
		
		return cell;
	}
	
	public void deallocCell(bhBufferCell cell)
	{
		if( cell.getVisualization() != null )
		{
			m_delegate.destroyVisualization(cell.getVisualization());
		}
		
		cell.clear(); // allow gc to do it's thing

		m_pool.deallocate(cell);
	}
}