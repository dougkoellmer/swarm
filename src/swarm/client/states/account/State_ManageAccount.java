package swarm.client.states.account;


import swarm.client.app.smAppContext;
import swarm.client.managers.smClientAccountManager;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidator;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;



/**
 * ...
 * @author 
 */
public class State_ManageAccount extends smA_State
{
	public State_ManageAccount(smClientAccountManager accountMngr)
	{
		smA_Action.register(new Action_ManageAccount_SignOut(accountMngr));
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
}