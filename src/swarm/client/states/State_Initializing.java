package swarm.client.states;

import swarm.client.app.smAppContext;
import swarm.client.app.smPlatformInfo;
import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.transaction.smE_RequestSynchronicity;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smInlineRequestDispatcher;

import swarm.server.account.smAccountDatabase.E_PasswordType;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.reflection.smI_Callback;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_Initializing extends smA_State implements smI_TransactionResponseHandler
{
	private int m_successCount = 0;
	
	private int m_requiredSuccessCount = 0;
	
	private final smAppContext m_appContext;
	
	public State_Initializing(smAppContext appContext)
	{
		m_appContext = appContext;
	}
	
	@Override
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		smE_ResponseSuccessControl control = smE_ResponseSuccessControl.CONTINUE;
		
		if ( request.getPath() == smE_RequestPath.getUserData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == smE_RequestPath.getGridData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == smE_RequestPath.getStartingPosition )
		{
			m_successCount++;
		}
		
		return smE_ResponseSuccessControl.CONTINUE;
	}
	
	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		return smE_ResponseErrorControl.CONTINUE; // bubble all errors up to base controller for error dialog.
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		final smClientAccountManager accountManager = m_appContext.accountMngr;
		final smUserManager userManager = m_appContext.userMngr;
		final smGridManager gridManager = m_appContext.gridMngr;
		final smClientTransactionManager transactionManager = m_appContext.txnMngr;
		
		//--- DRK > Do an initial transaction to see if user is signed in...this is synchronous.
		accountManager.init(new smI_Callback()
		{
			@Override
			public void invoke()
			{
				m_successCount = 0;
				m_requiredSuccessCount = 2;
				
				transactionManager.addHandler(State_Initializing.this);
				
				gridManager.getGridData(smE_TransactionAction.QUEUE_REQUEST);
				userManager.getPosition(smE_TransactionAction.QUEUE_REQUEST);
				
				if( accountManager.isSignedIn() )
				{
					userManager.populateUser(smE_TransactionAction.QUEUE_REQUEST);
					
					//m_requiredSuccessCount++;
				}

				transactionManager.flushRequestQueue();
			}
		});
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
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
			
			StateMachine_Base baseController = getContext().getEnteredState(StateMachine_Base.class);
			
			smClientAccountManager accountManager = m_appContext.accountMngr;
			smClientAccountManager.E_PasswordChangeTokenState resetTokenState = accountManager.getPasswordChangeTokenState();
			
			if( resetTokenState == smClientAccountManager.E_PasswordChangeTokenState.INVALID )
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Sorry!",
					"The time window for confirming your password change has either expired or was otherwise invalidated.<br><br>Please try again."
				);
				
				baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
					
				return;
			}
			else if( resetTokenState == smClientAccountManager.E_PasswordChangeTokenState.VALID )
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
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		final smClientTransactionManager transactionManager = m_appContext.txnMngr;
		transactionManager.removeHandler(this);
		
		m_successCount = 0;
	}
}


