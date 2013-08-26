package swarm.client.states.account;


import swarm.client.app.sm_c;
import swarm.client.managers.smClientAccountManager;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidator;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;



/**
 * ...
 * @author 
 */
public class State_ManageAccount extends smA_State
{
	public static class SignOut extends smA_Action
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			bhClientAccountManager accountManager = sm_c.accountMngr;
			accountManager.signOut();
			
			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);			
		}

		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_ManageAccount.class;
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			bhClientAccountManager accountManager = sm_c.accountMngr;
			return accountManager.isSignedIn();
		}
	}
	
	public State_ManageAccount()
	{
		smA_Action.register(new SignOut());
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
}