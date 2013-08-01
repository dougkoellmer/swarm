package com.b33hive.client.states.account;


import com.b33hive.client.managers.bhClientAccountManager;
import com.b33hive.client.managers.bhGridManager;
import com.b33hive.client.managers.bhUserManager;
import com.b33hive.client.managers.bhClientAccountManager.E_ResponseType;
import com.b33hive.client.managers.bhClientAccountManager.I_Delegate;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.client.transaction.bhE_ResponseErrorControl;
import com.b33hive.client.transaction.bhE_ResponseSuccessControl;
import com.b33hive.client.transaction.bhI_TransactionResponseHandler;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.shared.account.bhE_SignInValidationError;
import com.b33hive.shared.account.bhI_SignInCredentialValidator;
import com.b33hive.shared.account.bhSignInCredentials;
import com.b33hive.shared.account.bhSignInValidator;
import com.b33hive.shared.account.bhSignUpCredentials;
import com.b33hive.shared.account.bhSignUpValidationResult;
import com.b33hive.shared.account.bhSignUpValidator;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_ActionArgs;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
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
			
			bhClientAccountManager manager = bhClientAccountManager.getInstance();
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

			bhClientAccountManager.getInstance().signIn(creds, bhE_TransactionAction.QUEUE_REQUEST);
			bhUserManager.getInstance().populateUser(bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH);
			
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

			bhClientAccountManager.getInstance().signUp(creds, bhE_TransactionAction.QUEUE_REQUEST);
			bhUserManager.getInstance().populateUser(bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH);

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