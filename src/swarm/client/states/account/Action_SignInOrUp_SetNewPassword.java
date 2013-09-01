package swarm.client.states.account;

import swarm.client.managers.smClientAccountManager;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_SignInOrUp_SetNewPassword extends smA_Action
{
	private final smClientAccountManager m_accountMngr;
	
	Action_SignInOrUp_SetNewPassword(smClientAccountManager accountMngr)
	{
		m_accountMngr = accountMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		smSignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;
		
		smClientAccountManager manager = m_accountMngr;
		manager.setNewDesiredPassword(creds);
		
		machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);	
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		return State_SignInOrUp.isSignInOrResetPerformable(args, true);
	}
}