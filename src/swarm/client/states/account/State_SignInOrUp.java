package swarm.client.states.account;


import swarm.client.app.AppContext;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.managers.ClientAccountManager.E_ResponseType;
import swarm.client.managers.ClientAccountManager.I_Delegate;

import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.account.E_SignInValidationError;
import swarm.shared.account.I_SignInCredentialValidator;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidator;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class State_SignInOrUp extends A_State
{
	private final ClientAccountManager m_accountMngr;
	
	public State_SignInOrUp(ClientAccountManager accountMngr, UserManager userMngr)
	{
		m_accountMngr = accountMngr;
		
		register(new Action_SignInOrUp_SignIn(accountMngr, userMngr));
		register(new Action_SignInOrUp_SignUp(accountMngr, userMngr));
		register(new Action_SignInOrUp_SetNewPassword(accountMngr));
	}
	
	boolean isSignInOrResetPerformable(StateArgs args, boolean isForNewPassword)
	{
		//--- DRK > Just a final double-check catch-all here...UI should probably have completely validated before performing the action.
		
		SignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;
		
		if( isForNewPassword )
		{
			creds.setIsForNewPassword(true);
		}
		
		boolean everythingOk = m_accountMngr.getSignInValidator().validate(creds).isEverythingOk();
		
		U_Debug.ASSERT(everythingOk, "SignIn1");
		
		return everythingOk;
	}
	
	@Override 
	protected void willExit()
	{
	}
}