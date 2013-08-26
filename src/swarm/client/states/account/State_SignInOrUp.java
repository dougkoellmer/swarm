package swarm.client.states.account;


import swarm.client.app.sm_c;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.managers.smClientAccountManager.E_ResponseType;
import swarm.client.managers.smClientAccountManager.I_Delegate;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.account.smE_SignInValidationError;
import swarm.shared.account.smI_SignInCredentialValidator;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidator;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smSignUpValidationResult;
import swarm.shared.account.smSignUpValidator;
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
	public static class SetNewPassword extends smA_Action
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			smSignInCredentials creds = ((SignIn.Args) args).m_creds;
			
			smClientAccountManager manager = sm_c.accountMngr;
			manager.setNewDesiredPassword(creds);
			
			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);	
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			return isSignInOrResetPerformable(args, true);
		}

		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_SignInOrUp.class;
		}
	}
	
	public static class SignIn extends smA_Action
	{
		public static class Args extends smA_ActionArgs
		{
			private smSignInCredentials m_creds;
			
			public void setCreds(smSignInCredentials creds)
			{
				m_creds = creds;
			}
		}
		
		@Override
		public void perform(smA_ActionArgs args)
		{
			smSignInCredentials creds = ((Args) args).m_creds;

			smUserManager userManager = sm_c.userMngr;
			smClientAccountManager accountManager = sm_c.accountMngr;
			
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
			return isSignInOrResetPerformable(args, false);
		}
	}
	
	private static boolean isSignInOrResetPerformable(smA_ActionArgs args, boolean isForNewPassword)
	{
		//--- DRK > Just a final double-check catch-all here...UI should probably have completely validated before performing the action.
		
		smSignInCredentials creds = ((SignIn.Args) args).m_creds;
		
		if( isForNewPassword )
		{
			creds.setIsForNewPassword(true);
		}
		
		boolean everythingOk = smSignInValidator.getInstance().validate(creds).isEverythingOk();
		
		smU_Debug.ASSERT(everythingOk, "SignIn1");
		
		return everythingOk;
	}
	
	public static class SignUp extends smA_Action
	{
		public static class Args extends smA_ActionArgs
		{
			private smSignUpCredentials m_creds;
			
			public void setCreds(smSignUpCredentials creds)
			{
				m_creds = creds;
			}
		}
		
		@Override
		public void perform(smA_ActionArgs args)
		{
			smSignUpCredentials creds = ((Args) args).m_creds;

			smUserManager userManager = sm_c.userMngr;
			smClientAccountManager accountManager = sm_c.accountMngr;
			
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
			
			smSignUpCredentials creds = ((Args) args).m_creds;
			boolean everythingOk = smSignUpValidator.getInstance().validate(creds).isEverythingOk();
			
			smU_Debug.ASSERT(everythingOk, "SignUp1");
			
			return everythingOk;
		}
	}
	
	public State_SignInOrUp()
	{
		smA_Action.register(new State_SignInOrUp.SignIn());
		smA_Action.register(new State_SignInOrUp.SignUp());
		smA_Action.register(new State_SignInOrUp.SetNewPassword());
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