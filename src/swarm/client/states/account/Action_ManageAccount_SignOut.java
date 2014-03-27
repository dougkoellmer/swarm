package swarm.client.states.account;

import swarm.client.managers.ClientAccountManager;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_ManageAccount_SignOut extends A_Action
{
	private final ClientAccountManager m_accountMngr;
	
	Action_ManageAccount_SignOut(ClientAccountManager accountMngr)
	{
		m_accountMngr = accountMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		m_accountMngr.signOut();
		
		pushVer(this.getState().getParent(), State_AccountStatusPending.class);			
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		return m_accountMngr.isSignedIn();
	}
}