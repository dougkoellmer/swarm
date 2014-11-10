package swarm.client.states.camera;

import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_Action_Event;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;

public class Event_Camera_OnCellSizeFound extends A_Action_Event
{
	public static class Args extends StateArgs
	{
		private final CellSize m_cellSize = new CellSize();
		private final CellAddressMapping m_mapping = new CellAddressMapping();
		
		Args()
		{
		}
		
		void init(CellSize size_copied, CellAddressMapping mapping_copied)
		{
			m_cellSize.copy(size_copied);
			m_mapping.copy(mapping_copied);
		}

		public CellSize getCellSize()
		{
			return m_cellSize;
		}
		
		public CellAddressMapping getMapping()
		{
			return m_mapping;
		}
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}