package swarm.client.view.cell;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Widget;

import swarm.client.entities.smI_BufferCellListener;
import swarm.client.structs.smI_CellPoolDelegate;
import swarm.client.view.sandbox.smSandboxManager;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.memory.smObjectPool;
import swarm.shared.reflection.smI_Class;

public class smVisualCellPool implements smI_CellPoolDelegate
{
	private static final class CustomPool extends smObjectPool<smVisualCell>
	{
		public CustomPool(smI_Class<smVisualCell> type)
		{
			super(type);
		}
		
		void cleanCode()
		{
			for( int i = getAllocCount(); i < this.m_instances.size(); i++ )
			{
				smVisualCell cell = this.m_instances.get(i);
				
				if( cell.getParent() != null )
				{
					cell.removeFromParent();
				}
				
				cell.showEmptyContent();
			}
		}
	}
	
	private final smI_Class<smVisualCell> m_visualCellClass;
	
	private final CustomPool m_pool;
	
	private final ArrayList<smVisualCell> m_queuedRemovals = new ArrayList<smVisualCell>();
	
	private final Widget m_cellContainer;
	
	private int m_queueIndex = 0;
	
	private int m_maxQueueSize = 40;
	
	private boolean m_poolNeedsCleaning = false;
	
	smVisualCellPool(final smSandboxManager sandboxMngr, Widget cellContainer)
	{
		m_cellContainer = cellContainer;
		
		m_visualCellClass = new smI_Class<smVisualCell>()
		{
			@Override
			public smVisualCell newInstance()
			{
				return new smVisualCell(sandboxMngr);
			}
		};
		
		m_pool = new CustomPool(m_visualCellClass);
	}
	
	public smI_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim)
	{
		smVisualCell newVisualCell = m_pool.allocate();
		newVisualCell.setVisible(true);
		
		//s_logger.severe("Creating: " + newVisualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
		
		if( newVisualCell.getParent() != null )
		{
			//--- DRK > Invalid assert...for now cells' visibility is just toggled.
			//smU_Debug.ASSERT(false, "createVisualization1");
		}
		
		newVisualCell.onCreate(width, height, padding, subCellDim);
		
		return newVisualCell;
	}
	
	public void destroyVisualization(smI_BufferCellListener visualization)
	{
		smVisualCell visualCell = (smVisualCell) visualization;
		
		//s_logger.severe("Destroying: " + visualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
		
		if( visualCell.getParent() != m_cellContainer )
		{
			//--- DRK > Fringe case can hit this assert, so it's removed...
			//---		You flick the camera so it comes to rest right at the edge of a new cell, creating the visualization.
			//---		Then inside the same update loop, you do a mouse press which, probably due to numerical error, 
			//---		takes the new cell out of view. Currently, in the state machine implementation, that mouse press
			//---		forces a refresh of the cell buffer in the same update loop, right after the refresh that caused the 
			//---		visualization to get created. Because this manager lazily adds/removes cells, the cell didn't have a
			//---		chance to get a parent.
			//smU_Debug.ASSERT(false, "destroyVisualization1....bad parent: " + visualCell.getParent());
		}
		
		visualCell.setVisible(false);
		destroyVisualCell(visualCell);
	}
	
	public void cleanPool()
	{
		if( !m_poolNeedsCleaning )  return;
		
		m_pool.cleanCode();
		
		m_poolNeedsCleaning = false;
	}
	
	private void destroyVisualCell(smVisualCell cell)
	{
		if( smE_CodeSafetyLevel.isStatic(cell.getCodeSafetyLevel()) )
		{
			m_poolNeedsCleaning = true;
		}
		
		cell.onDestroy();
		
		if( cell.getParent() != null )
		{
			smU_Debug.ASSERT(cell.getParent() == m_cellContainer, "processRemovals1");
			
			//--- DRK > No real need to remove from parent I think.
			//---		It's just invisible, and will probably be added back eventually anyway.
			//cell.removeFromParent();
		}
		
		m_pool.deallocate(cell);
	}
	
	private void flushDestroyQueue(int destroyLimit)
	{
		int i = 0;
		
		for( i = m_queueIndex; i < m_queuedRemovals.size() && i < m_queueIndex + destroyLimit; i++ )
		{
			smVisualCell ithCell = m_queuedRemovals.get(i);
			m_queuedRemovals.set(i, null);
			
			destroyVisualCell(ithCell);
		}
		
		if( i == m_queuedRemovals.size() )
		{
			m_queueIndex = 0;
			m_queuedRemovals.clear();
		}
		else
		{
			m_queueIndex = i;
		}
	}
}
