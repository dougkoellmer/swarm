package swarm.client.states.account;


import swarm.client.app.smAppContext;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.managers.smClientAccountManager.E_ResponseType;
import swarm.client.managers.smClientAccountManager.I_Delegate;

import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.account.smE_SignInValidationError;
import swarm.shared.account.smI_SignInCredentialValidator;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidator;
import swarm.shared.account.smSignUpValidationResult;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class State_SignInOrUp extends smA_State
{
	static boolean isSignInOrResetPerformable(smA_ActionArgs args, boolean isForNewPassword)
	{
		//--- DRK > Just a final double-check catch-all here...UI should probably have completely validated before performing the action.
		
		smSignInCredentials creds = ((Action_SignInOrUp_SignIn.Args) args).m_creds;
		
		if( isForNewPassword )
		{
			creds.setIsForNewPassword(true);
		}
		
		boolean everythingOk = smSignInValidator.getInstance().validate(creds).isEverythingOk();
		
		smU_Debug.ASSERT(everythingOk, "SignIn1");
		
		return everythingOk;
	}
	
	public State_SignInOrUp(smClientAccountManager accountMngr, smUserManager userMngr)
	{
		smA_Action.register(new Action_SignInOrUp_SignIn(accountMngr, userMngr));
		smA_Action.register(new Action_SignInOrUp_SignUp(accountMngr, userMngr));
		smA_Action.register(new Action_SignInOrUp_SetNewPassword(accountMngr));
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		
	}
	
	@Override 
	protected void willExit()
	{
	}
}