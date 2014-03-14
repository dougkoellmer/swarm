package swarm.client.states.account;

import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.UserManager;
import swarm.client.transaction.E_TransactionAction;
import swarm.shared.account.SignInCredentials;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_SignInOrUp_SignIn extends A_Action
{
	public static class Args extends StateArgs
	{
		SignInCredentials m_creds;
		
		public void setCreds(SignInCredentials creds)
		{
			m_creds = creds;
		}
	}
	
	private final ClientAccountManager m_accountMngr;
	private final UserManager m_userMngr;
	
	Action_SignInOrUp_SignIn(ClientAccountManager accountMngr, UserManager userMngr)
	{
		m_accountMngr = accountMngr;
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		SignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;

		UserManager userManager = m_userMngr;
		ClientAccountManager accountManager = m_accountMngr;
		
		accountManager.signIn(creds, E_TransactionAction.QUEUE_REQUEST);
		userManager.populateUser(E_TransactionAction.QUEUE_REQUEST_AND_FLUSH);
		
		pushState(this.getState().getParent(), State_AccountStatusPending.class);			
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		State_SignInOrUp state = this.getState();
		
		return state.isSignInOrResetPerformable(args, false);
	}
}