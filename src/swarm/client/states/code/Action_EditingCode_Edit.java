package swarm.client.states.code;

import swarm.client.app.smAppContext;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smBufferCell;
import swarm.client.managers.smUserManager;
import swarm.client.states.camera.State_ViewingCell;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_EditingCode_Edit extends smA_Action 
{
	public static class Args extends smA_ActionArgs
	{
		private String m_changedCode = null;
		
		public void init(String changedCode)
		{
			m_changedCode = changedCode;
		}
	}
	
	private final smUserManager m_userMngr;
	
	Action_EditingCode_Edit(smUserManager userMngr)
	{
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		String code = ((Action_EditingCode_Edit.Args) args).m_changedCode;
		
		State_ViewingCell viewingState = getContext().getForegroundedState(State_ViewingCell.class);
		smBufferCell viewedCell = viewingState.getCell();
		
		smA_ClientUser user = m_userMngr.getUser();
		
		user.onSourceCodeChanged(viewedCell.getCoordinate(), code);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		State_ViewingCell state = getContext().getForegroundedState(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}

		//--- DRK > Just to make extra sure.
		smA_ClientUser user = m_userMngr.getUser();
		if( !user.isEditable(state.getCell().getCoordinate()))
		{
			return false;
		}
		
		return true;
	}
}