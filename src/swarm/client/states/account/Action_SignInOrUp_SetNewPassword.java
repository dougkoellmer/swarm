package swarm.client.states.account;

import swarm.client.managers.ClientAccountManager;
import swarm.shared.account.SignInCredentials;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_SignInOrUp_SetNewPassword extends A_Action
{
	private final ClientAccountManager m_accountMngr;
	
	Action_SignInOrUp_SetNewPassword(ClientAccountManager accountMngr)
	{
		m_accountMngr = accountMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		SignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;
		
		ClientAccountManager manager = m_accountMngr;
		manager.setNewDesiredPassword(creds);
		
		pushState(this.getState().getParent(), State_AccountStatusPending.class);	
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		State_SignInOrUp state = this.getState();
		
		return state.isSignInOrResetPerformable(args, true);
	}
}