package swarm.client.states.account;

import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.transaction.smE_TransactionAction;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_SignInOrUp_SignIn extends smA_Action
{
	public static class Args extends smA_ActionArgs
	{
		smSignInCredentials m_creds;
		
		public void setCreds(smSignInCredentials creds)
		{
			m_creds = creds;
		}
	}
	
	private final smClientAccountManager m_accountMngr;
	private final smUserManager m_userMngr;
	
	Action_SignInOrUp_SignIn(smClientAccountManager accountMngr, smUserManager userMngr)
	{
		m_accountMngr = accountMngr;
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		smSignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;

		smUserManager userManager = m_userMngr;
		smClientAccountManager accountManager = m_accountMngr;
		
		accountManager.signIn(creds, smE_TransactionAction.QUEUE_REQUEST);
		userManager.populateUser(smE_TransactionAction.QUEUE_REQUEST_AND_FLUSH);
		
		machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);			
	}

	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return State_SignInOrUp.class;
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		return State_SignInOrUp.isSignInOrResetPerformable(args, false);
	}
}