package swarm.client.states;

import swarm.client.app.AppContext;
import swarm.client.app.PlatformInfo;
import swarm.client.entities.A_ClientUser;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.transaction.E_RequestSynchronicity;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.InlineRequestDispatcher;

import swarm.shared.debugging.U_Debug;
import swarm.shared.reflection.I_Callback;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_Initializing extends A_State implements I_TransactionResponseHandler
{
	private int m_successCount = 0;
	
	private int m_requiredSuccessCount = 0;
	
	private final AppContext m_appContext;
	
	public State_Initializing(AppContext appContext)
	{
		m_appContext = appContext;
	}
	
	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		E_ResponseSuccessControl control = E_ResponseSuccessControl.CONTINUE;
		
		if ( request.getPath() == E_RequestPath.getUserData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == E_RequestPath.getGridData )
		{
			m_successCount++;
		}
		else if ( request.getPath() == E_RequestPath.getStartingPosition )
		{
			m_successCount++;
		}
		
		return E_ResponseSuccessControl.CONTINUE;
	}
	
	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		return E_ResponseErrorControl.CONTINUE; // bubble all errors up to base controller for error dialog.
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
		final ClientAccountManager accountManager = m_appContext.accountMngr;
		final UserManager userManager = m_appContext.userMngr;
		final GridManager gridManager = m_appContext.gridMngr;
		final ClientTransactionManager transactionManager = m_appContext.txnMngr;
		
		//--- DRK > Do an initial transaction to see if user is signed in.
		accountManager.init(new I_Callback()
		{
			@Override
			public void invoke(Object ... args)
			{
				m_successCount = 0;
				m_requiredSuccessCount = 2;
				
				transactionManager.addHandler(State_Initializing.this);
				
				gridManager.getGridData(E_TransactionAction.QUEUE_REQUEST);
				userManager.getPosition(E_TransactionAction.QUEUE_REQUEST);
				
				if( accountManager.isSignedIn() )
				{
					userManager.populateUser(E_TransactionAction.QUEUE_REQUEST);
					
					//m_requiredSuccessCount++;
				}

				transactionManager.flushAsyncRequestQueue();
			}
		});
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
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
			
			ClientAccountManager accountManager = m_appContext.accountMngr;
			ClientAccountManager.E_PasswordChangeTokenState resetTokenState = accountManager.getPasswordChangeTokenState();
			
			if( resetTokenState == ClientAccountManager.E_PasswordChangeTokenState.INVALID )
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Sorry!",
					"The time window for confirming your password change has either expired or was otherwise invalidated.<br><br>Please try again."
				);
				
				baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
					
				return;
			}
			else if( resetTokenState == ClientAccountManager.E_PasswordChangeTokenState.VALID )
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
	protected void willBackground(Class<? extends A_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		final ClientTransactionManager transactionManager = m_appContext.txnMngr;
		transactionManager.removeHandler(this);
		
		m_successCount = 0;
	}
}


