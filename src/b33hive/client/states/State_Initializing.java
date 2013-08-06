package b33hive.client.states;

import b33hive.client.app.bhPlatformInfo;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhClientGrid;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhGridManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.transaction.bhE_RequestSynchronicity;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.transaction.bhInlineRequestDispatcher;

import b33hive.server.account.bhAccountDatabase.E_PasswordType;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.reflection.bhI_Callback;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_Initializing extends bhA_State implements bhI_TransactionResponseHandler
{
	private int m_successCount = 0;
	
	private int m_requiredSuccessCount = 0;
	
	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		bhE_ResponseSuccessControl control = bhE_ResponseSuccessControl.CONTINUE;
		
		if ( request.getPath() == bhE_RequestPath.getUserData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == bhE_RequestPath.getGridData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == bhE_RequestPath.getStartingPosition )
		{
			m_successCount++;
		}
		
		return bhE_ResponseSuccessControl.CONTINUE;
	}
	
	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		return bhE_ResponseErrorControl.CONTINUE; // bubble all errors up to base controller for error dialog.
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		final bhClientAccountManager accountManager = bh_c.accountMngr;
		final bhUserManager userManager = bh_c.userMngr;
		final bhGridManager gridManager = bh_c.gridMngr;
		final bhClientTransactionManager transactionManager = bh_c.txnMngr;
		
		//--- DRK > Do an initial transaction to see if user is signed in...this is synchronous.
		accountManager.init(new bhI_Callback()
		{
			@Override
			public void invoke()
			{
				m_successCount = 0;
				m_requiredSuccessCount = 2;
				
				transactionManager.addHandler(State_Initializing.this);
				
				gridManager.getGridData(bhE_TransactionAction.QUEUE_REQUEST);
				userManager.getPosition(bhE_TransactionAction.QUEUE_REQUEST);
				
				if( accountManager.isSignedIn() )
				{
					userManager.populateUser(bhE_TransactionAction.QUEUE_REQUEST);
					
					//m_requiredSuccessCount++;
				}

				transactionManager.flushRequestQueue();
			}
		});
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( revealingState != null )
		{			
			tryEnteringApp();
			
			return;
		}
	}
	
	private void tryEnteringApp()
	{
		if( this.isForegrounded() && m_successCount >= m_requiredSuccessCount )
		{
			machine_setState(getParent(), StateContainer_Base.class);
			
			StateMachine_Base baseController = bhA_State.getEnteredInstance(StateMachine_Base.class);
			
			bhClientAccountManager accountManager = bh_c.accountMngr;
			bhClientAccountManager.E_PasswordChangeTokenState resetTokenState = accountManager.getPasswordChangeTokenState();
			
			if( resetTokenState == bhClientAccountManager.E_PasswordChangeTokenState.INVALID )
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Sorry!",
					"The window for confirming your password change has either expired or was otherwise invalidated.<br><br>Please try again."
				);
				
				baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
					
				return;
			}
			else if( resetTokenState == bhClientAccountManager.E_PasswordChangeTokenState.VALID )
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Almost Done!",
					"To confirm your new password, please sign in normally using the new password."
				);
				
				baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
				
				return;
			}
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
		//--- DRK > We force one update of this state so that transaction response handlers higher up the stack
		//---		have a chance to do their response logic.  If we put this directly in the response handler for
		//---		this state, than we could enter the "main" states of the machine before all necessary entities are initialized.
		tryEnteringApp();
	}
	
	@Override
	protected void willBackground(Class<? extends bhA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		final bhClientTransactionManager transactionManager = bh_c.txnMngr;
		transactionManager.removeHandler(this);
		
		m_successCount = 0;
	}
}


