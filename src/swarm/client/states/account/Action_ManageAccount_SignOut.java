package swarm.client.states.account;

import swarm.client.managers.smClientAccountManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_ManageAccount_SignOut extends smA_Action
{
	private final smClientAccountManager m_accountMngr;
	
	Action_ManageAccount_SignOut(smClientAccountManager accountMngr)
	{
		m_accountMngr = accountMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		m_accountMngr.signOut();
		
		machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);			
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		return m_accountMngr.isSignedIn();
	}
}