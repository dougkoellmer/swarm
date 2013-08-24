package swarm.client.states.account;


import swarm.client.app.sm_c;
import swarm.client.managers.bhClientAccountManager;
import swarm.shared.account.bhSignInCredentials;
import swarm.shared.account.bhSignInValidator;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateConstructor;



/**
 * ...
 * @author 
 */
public class State_ManageAccount extends bhA_State
{
	public static class SignOut extends bhA_Action
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhClientAccountManager accountManager = sm_c.accountMngr;
			accountManager.signOut();
			
			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);			
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_ManageAccount.class;
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			bhClientAccountManager accountManager = sm_c.accountMngr;
			return accountManager.isSignedIn();
		}
	}
	
	public State_ManageAccount()
	{
		bhA_Action.register(new SignOut());
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
	}
}