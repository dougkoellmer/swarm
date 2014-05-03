package swarm.client.states.code;

import swarm.client.app.AppContext;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.BufferCell;
import swarm.client.managers.UserManager;
import swarm.client.states.camera.State_ViewingCell;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_EditingCode_Edit extends A_Action 
{
	public static class Args extends StateArgs
	{
		private String m_changedCode = null;
		
		public void init(String changedCode)
		{
			m_changedCode = changedCode;
		}
	}
	
	private final UserManager m_userMngr;
	
	Action_EditingCode_Edit(UserManager userMngr)
	{
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		String code = ((Action_EditingCode_Edit.Args) args).m_changedCode;
		
		State_ViewingCell viewingState = getContext().getForegrounded(State_ViewingCell.class);
		BufferCell viewedCell = viewingState.getCell();
		
		A_ClientUser user = m_userMngr.getUser();
		
		user.onSourceCodeChanged(viewedCell.getCoordinate(), code);
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		State_ViewingCell state = getContext().getForegrounded(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}

		//--- DRK > Just to make extra sure.
		A_ClientUser user = m_userMngr.getUser();
		if( !user.isEditable(state.getCell().getCoordinate()))
		{
			return false;
		}
		
		return true;
	}
}