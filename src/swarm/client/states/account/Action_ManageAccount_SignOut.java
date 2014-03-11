package swarm.client.states.account;

import swarm.client.managers.ClientAccountManager;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;

public class Action_ManageAccount_SignOut extends A_Action
{
	private final ClientAccountManager m_accountMngr;
	
	Action_ManageAccount_SignOut(ClientAccountManager accountMngr)
	{
		m_accountMngr = accountMngr;
	}
	
	@Override
	public void perform(A_ActionArgs args)
	{
		m_accountMngr.signOut();
		
		pushState(this.getState().getParent(), State_AccountStatusPending.class);			
	}
	
	@Override
	public boolean isPerformable(A_ActionArgs args)
	{
		return m_accountMngr.isSignedIn();
	}
}