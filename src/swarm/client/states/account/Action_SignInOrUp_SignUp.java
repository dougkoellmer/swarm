package swarm.client.states.account;


import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.transaction.smE_TransactionAction;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smSignUpValidator;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_SignInOrUp_SignUp extends smA_Action
{
	public static class Args extends smA_ActionArgs
	{
		private smSignUpCredentials m_creds;
		
		public void setCreds(smSignUpCredentials creds)
		{
			m_creds = creds;
		}
	}
	
	private final smClientAccountManager m_accountMngr;
	
	private final smUserManager m_userMngr;
	
	Action_SignInOrUp_SignUp(smClientAccountManager accountMngr, smUserManager userMngr)
	{
		m_accountMngr = accountMngr;
		m_userMngr = userMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		smSignUpCredentials creds = ((Action_SignInOrUp_SignUp.Args) args).m_creds;

		smUserManager userManager = m_userMngr;
		smClientAccountManager accountManager = m_accountMngr;
		
		accountManager.signUp(creds, smE_TransactionAction.QUEUE_REQUEST);
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
		//--- DRK > Just a final double-check catch-all here...UI should have completely validated before performing the action.
		
		smSignUpCredentials creds = ((Action_SignInOrUp_SignUp.Args) args).m_creds;
		boolean everythingOk = smSignUpValidator.getInstance().validate(creds).isEverythingOk();
		
		smU_Debug.ASSERT(everythingOk, "SignUp1");
		
		return everythingOk;
	}
}