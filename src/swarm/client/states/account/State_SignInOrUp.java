package swarm.client.states.account;


import swarm.client.app.sm_c;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhGridManager;
import swarm.client.managers.bhUserManager;
import swarm.client.managers.bhClientAccountManager.E_ResponseType;
import swarm.client.managers.bhClientAccountManager.I_Delegate;
import swarm.client.transaction.bhE_TransactionAction;
import swarm.client.transaction.bhE_ResponseErrorControl;
import swarm.client.transaction.bhE_ResponseSuccessControl;
import swarm.client.transaction.bhI_TransactionResponseHandler;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.shared.account.bhE_SignInValidationError;
import swarm.shared.account.bhI_SignInCredentialValidator;
import swarm.shared.account.bhSignInCredentials;
import swarm.shared.account.bhSignInValidator;
import swarm.shared.account.bhSignUpCredentials;
import swarm.shared.account.bhSignUpValidationResult;
import swarm.shared.account.bhSignUpValidator;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class State_SignInOrUp extends bhA_State
{
	public static class SetNewPassword extends bhA_Action
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhSignInCredentials creds = ((SignIn.Args) args).m_creds;
			
			bhClientAccountManager manager = sm_c.accountMngr;
			manager.setNewDesiredPassword(creds);
			
			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);	
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return isSignInOrResetPerformable(args, true);
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_SignInOrUp.class;
		}
	}
	
	public static class SignIn extends bhA_Action
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhSignInCredentials m_creds;
			
			public void setCreds(bhSignInCredentials creds)
			{
				m_creds = creds;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhSignInCredentials creds = ((Args) args).m_creds;

			bhUserManager userManager = sm_c.userMngr;
			bhClientAccountManager accountManager = sm_c.accountMngr;
			
			accountManager.signIn(creds, bhE_TransactionAction.QUEUE_REQUEST);
			userManager.populateUser(bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH);
			
			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);			
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_SignInOrUp.class;
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return isSignInOrResetPerformable(args, false);
		}
	}
	
	private static boolean isSignInOrResetPerformable(bhA_ActionArgs args, boolean isForNewPassword)
	{
		//--- DRK > Just a final double-check catch-all here...UI should probably have completely validated before performing the action.
		
		bhSignInCredentials creds = ((SignIn.Args) args).m_creds;
		
		if( isForNewPassword )
		{
			creds.setIsForNewPassword(true);
		}
		
		boolean everythingOk = bhSignInValidator.getInstance().validate(creds).isEverythingOk();
		
		bhU_Debug.ASSERT(everythingOk, "SignIn1");
		
		return everythingOk;
	}
	
	public static class SignUp extends bhA_Action
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhSignUpCredentials m_creds;
			
			public void setCreds(bhSignUpCredentials creds)
			{
				m_creds = creds;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhSignUpCredentials creds = ((Args) args).m_creds;

			bhUserManager userManager = sm_c.userMngr;
			bhClientAccountManager accountManager = sm_c.accountMngr;
			
			accountManager.signUp(creds, bhE_TransactionAction.QUEUE_REQUEST);
			userManager.populateUser(bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH);

			machine_pushState(this.getState().getParent(), State_AccountStatusPending.class);	
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_SignInOrUp.class;
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			//--- DRK > Just a final double-check catch-all here...UI should have completely validated before performing the action.
			
			bhSignUpCredentials creds = ((Args) args).m_creds;
			boolean everythingOk = bhSignUpValidator.getInstance().validate(creds).isEverythingOk();
			
			bhU_Debug.ASSERT(everythingOk, "SignUp1");
			
			return everythingOk;
		}
	}
	
	public State_SignInOrUp()
	{
		bhA_Action.register(new State_SignInOrUp.SignIn());
		bhA_Action.register(new State_SignInOrUp.SignUp());
		bhA_Action.register(new State_SignInOrUp.SetNewPassword());
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		
	}
	
	@Override 
	protected void willExit()
	{
	}
}