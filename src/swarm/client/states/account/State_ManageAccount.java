package swarm.client.states.account;


import swarm.client.app.AppContext;
import swarm.client.managers.ClientAccountManager;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidator;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateArgs;



/**
 * ...
 * @author 
 */
public class State_ManageAccount extends A_State
{
	public State_ManageAccount(ClientAccountManager accountMngr)
	{
		registerAction(new Action_ManageAccount_SignOut(accountMngr));
	}
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
	}
}