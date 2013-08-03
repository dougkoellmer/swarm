package b33hive.client.states.account;


import b33hive.client.managers.bhClientAccountManager;
import b33hive.shared.account.bhSignInCredentials;
import b33hive.shared.account.bhSignInValidator;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_ActionArgs;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateConstructor;


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
			bhClientAccountManager.getInstance().signOut();
			
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
			return bhClientAccountManager.getInstance().isSignedIn();
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