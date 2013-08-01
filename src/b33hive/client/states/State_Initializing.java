package com.b33hive.client.states;

import com.b33hive.client.app.bhPlatformInfo;
import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.entities.bhClientUser;
import com.b33hive.client.managers.bhClientAccountManager;
import com.b33hive.client.managers.bhGridManager;
import com.b33hive.client.managers.bhUserManager;
import com.b33hive.client.transaction.bhE_RequestSynchronicity;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.client.transaction.bhE_ResponseErrorControl;
import com.b33hive.client.transaction.bhE_ResponseSuccessControl;
import com.b33hive.client.transaction.bhGwtRequestDispatcher;
import com.b33hive.client.transaction.bhI_TransactionResponseHandler;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.client.transaction.bhInlineRequestDispatcher;

import com.b33hive.server.account.bhAccountDatabase.E_PasswordType;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.reflection.bhI_Callback;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
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
		//--- DRK > Do an initial transaction to see if user is signed in...this is synchronous.
		bhClientAccountManager.getInstance().init(new bhI_Callback()
		{
			@Override
			public void invoke()
			{
				m_successCount = 0;
				m_requiredSuccessCount = 2;
				
				bhClientTransactionManager.getInstance().addHandler(State_Initializing.this);
				
				bhGridManager.getInstance().getGridData(bhE_TransactionAction.QUEUE_REQUEST);
				bhUserManager.getInstance().getPosition(bhE_TransactionAction.QUEUE_REQUEST);
				
				if( bhClientAccountManager.getInstance().isSignedIn() )
				{
					bhUserManager.getInstance().populateUser(bhE_TransactionAction.QUEUE_REQUEST);
					
					//m_requiredSuccessCount++;
				}

				bhClientTransactionManager.getInstance().flushRequestQueue();
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
				
			bhClientAccountManager.E_PasswordChangeTokenState resetTokenState = bhClientAccountManager.getInstance().getPasswordChangeTokenState();
			
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
		bhClientTransactionManager.getInstance().removeHandler(this);
		
		m_successCount = 0;
	}
}


