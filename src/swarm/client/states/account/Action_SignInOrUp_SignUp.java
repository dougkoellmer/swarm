package swarm.client.states.account;


import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.UserManager;
import swarm.client.transaction.E_TransactionAction;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidator;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_SignInOrUp_SignUp extends A_Action
{
	public static class Args extends StateArgs
	{
		private SignUpCredentials m_creds;
		
		public void setCreds(SignUpCredentials creds)
		{
			m_creds = creds;
		}
	}
	
	private final ClientAccountManager m_accountMngr;
	
	private final UserManager m_userMngr;
	
	Action_SignInOrUp_SignUp(ClientAccountManager accountMngr, UserManager userMngr)
	{
		m_accountMngr = accountMngr;
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		SignUpCredentials creds = ((Action_SignInOrUp_SignUp.Args) args).m_creds;

		UserManager userManager = m_userMngr;
		ClientAccountManager accountManager = m_accountMngr;
		
		accountManager.signUp(creds, E_TransactionAction.QUEUE_REQUEST);
		userManager.populateUser(E_TransactionAction.QUEUE_REQUEST_AND_FLUSH);

		pushVer(this.getState().getParent(), State_AccountStatusPending.class);	
	}

	@Override
	public boolean isPerformable(StateArgs args)
	{
		//--- DRK > Just a final double-check catch-all here...UI should have completely validated before performing the action.
		
		SignUpCredentials creds = ((Action_SignInOrUp_SignUp.Args) args).m_creds;
		boolean everythingOk = m_accountMngr.getSignUpValidator().validate(creds).isEverythingOk();
		
		U_Debug.ASSERT(everythingOk, "SignUp1");
		
		return everythingOk;
	}
}