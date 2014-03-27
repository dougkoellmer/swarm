package swarm.client.states;

import java.util.ArrayList;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellBufferManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.managers.ClientAccountManager.E_ResponseType;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_ResponseBatchListener;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.code.CompilerMessage;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;

import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_EventAction;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.StateContext;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.E_TelemetryRequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


/**
 * ...
 * @author 
 */
public class StateMachine_Base extends A_StateMachine implements I_TransactionResponseHandler
{
	/*public static class PushDialog extends smA_Action
	{
		@Override
		public void perform(Object[] args)
		{
			machine_pushState(this.getState(), State_GenericDialog.class, args[0]);
		}
	}*/
	
	public static class OnAccountManagerResponse extends A_EventAction
	{
		public static class Args extends StateArgs
		{
			private final ClientAccountManager.E_ResponseType m_type;
			
			public Args(ClientAccountManager.E_ResponseType type)
			{
				m_type = type;
			}
			
			public ClientAccountManager.E_ResponseType getType()
			{
				return m_type;
			}
		}
	}
	
	public static class OnUserPopulated extends A_EventAction
	{
	}
	
	public static class OnUserCleared extends A_EventAction
	{
	}
	
	public static class OnGridUpdate extends A_EventAction
	{
	}
	
	private static class BatchListener implements I_ResponseBatchListener
	{
		private final StateMachine_Base m_baseController;
		
		BatchListener(StateMachine_Base baseController)
		{
			m_baseController = baseController;
		}
		
		@Override
		public void onResponseBatchStart()
		{
			m_baseController.m_hasShownVersionMismatchDialog = false;
			m_baseController.m_hasShownGeneralTransactionErrorDialog = false;
		}

		@Override
		public void onResponseBatchEnd()
		{
		}
	}
	
	private static class DialogData
	{
		Class<? extends State_AsyncDialog> m_T__extends__State_AsyncDialog;
		State_GenericDialog.Constructor m_constructor;
		
		DialogData(Class<? extends State_AsyncDialog> T__extends__State_AsyncDialog, State_GenericDialog.Constructor constructor)
		{
			m_T__extends__State_AsyncDialog = T__extends__State_AsyncDialog;
			m_constructor = constructor;
		}
	}
	
	private static class AccountManagerDelegate implements ClientAccountManager.I_Delegate
	{
		private final StateMachine_Base m_state;
		
		AccountManagerDelegate(StateMachine_Base state)
		{
			m_state = state;
		}
		
		@Override
		public void onAccountTransactionResponse(E_ResponseType type)
		{
			OnAccountManagerResponse.Args args= new OnAccountManagerResponse.Args(type);
			m_state.perform(OnAccountManagerResponse.class, args);
			
			StateMachine_Base baseMachine = m_state.getContext().getForegroundedState(StateMachine_Base.class);
			
			if( type == E_ResponseType.PASSWORD_CHANGE_SUCCESS )
			{
				baseMachine.queueAsyncDialog
				(
					"Check Your E-mail",
					"We've registered your new password, but before it becomes active, you must follow the instructions in the e-mail we just sent. You have 30 minutes...go!<br><br>" +
					"Note that if we didn't find an account for the given e-mail address, then no e-mail was sent."
				);
			}
			else if( type == E_ResponseType.PASSWORD_CHANGE_FAILURE )
			{
				baseMachine.queueAsyncDialog
				(
					"Something Went Wrong",
					"A server error occured while trying to set your new password. Please try again later."
				);
			}
			else if( type == E_ResponseType.PASSWORD_CONFIRM_SUCCESS )
			{
				baseMachine.queueAsyncDialog
				(
					"All Done",
					"Your new password has been confirmed."
				);
			}
			else if( type == E_ResponseType.PASSWORD_CONFIRM_FAILURE )
			{
				baseMachine.queueAsyncDialog
				(
					"Something Went Wrong",
					"Most likely you incorrectly typed your new password. Please try again.<br><br>If you continue to experience issues, please contact support@b33hive.net."
				);
			}
		}

		@Override
		public void onAuthenticationError()
		{
			StateMachine_Base baseController = m_state.getContext().getEnteredState(StateMachine_Base.class);
			
			State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
			(
				"Sorry!",
				"Our system has lost track of you.  Please sign in again."
			);
			
			baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
		}
	}
	
	private final AccountManagerDelegate m_accountManagerDelegate;
	
	private final ArrayList<DialogData> m_asyncDialogQueue = new ArrayList<DialogData>();
	
	private boolean m_hasShownVersionMismatchDialog = false;
	private boolean m_hasShownGeneralTransactionErrorDialog = false;
	
	private final I_ResponseBatchListener m_batchListener = new BatchListener(this);
	
	private final AppContext m_appContext;
	
	public StateMachine_Base(AppContext appContext)
	{
		m_appContext = appContext;
		
		//smA_Action.register(new PushDialog());
		register(new OnGridUpdate());
		register(new OnAccountManagerResponse());
		
		register(new OnUserPopulated());
		register(new OnUserCleared());
		
		m_accountManagerDelegate = new AccountManagerDelegate(this);
	}
	
	void dequeueAsyncDialog()
	{
		m_asyncDialogQueue.remove(0);
		
		if( m_asyncDialogQueue.size() > 0 )
		{
			DialogData data = m_asyncDialogQueue.get(0);
			
			setState(this, data.m_T__extends__State_AsyncDialog, data.m_constructor);
		}
		else
		{
			popVer(this);
		}
	}
	
	public void queueAsyncDialog(String title, String body)
	{
		State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
		(
			title, body
		);
		
		this.queueAsyncDialog(State_AsyncDialog.class, constructor);
	}
	
	public void queueAsyncDialog(Class<? extends State_AsyncDialog> T, State_GenericDialog.Constructor constructor)
	{
		if( m_asyncDialogQueue.size() == 0 )
		{
			pushVer(this, T, constructor);
			
			m_asyncDialogQueue.add(null);
		}
		else
		{
			m_asyncDialogQueue.add(new DialogData(T, constructor));
		}
	}
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
		final ClientAccountManager accountManager = m_appContext.accountMngr;
		final UserManager userManager = m_appContext.userMngr;
		final GridManager gridManager = m_appContext.gridMngr;
		final ClientTransactionManager transactionManager = m_appContext.txnMngr;
		
		transactionManager.addHandler(this);
		transactionManager.addBatchListener(m_batchListener);
		
		accountManager.start();
		
		gridManager.start(new GridManager.I_Listener()
		{
			@Override
			public void onGridUpdate()
			{
				StateMachine_Base.this.perform(StateMachine_Base.OnGridUpdate.class);
			}
		});
		
		userManager.start(new UserManager.I_Listener()
		{
			@Override
			public void onUserPopulated()
			{
				StateMachine_Base.this.perform(OnUserPopulated.class);
			}

			@Override
			public void onUserDidClear()
			{
				StateMachine_Base.this.perform(OnUserCleared.class);
			}

			@Override
			public void onGetUserFailed()
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Our Bad!",
					"We couldn't retrieve your user information.  Please refresh your browser to try again."
				);
				
				StateMachine_Base.this.queueAsyncDialog(State_AsyncDialog.class, constructor);
			}
		});
		
		accountManager.addDelegate(m_accountManagerDelegate);
	}
	
	@Override
	protected void willExit()
	{
		final ClientAccountManager accountManager = m_appContext.accountMngr;
		final UserManager userManager = m_appContext.userMngr;
		final GridManager gridManager = m_appContext.gridMngr;
		final ClientTransactionManager transactionManager = m_appContext.txnMngr;
		
		accountManager.removeDelegate(m_accountManagerDelegate);
		
		userManager.stop();
		
		gridManager.stop();
		
		accountManager.stop();
		
		transactionManager.removeBatchListener(m_batchListener);
		
		transactionManager.removeHandler(this);
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
		if( this.getCurrentState() == null )
		{
			setState(this, State_Initializing.class);
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		return E_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() instanceof E_TelemetryRequestPath )
		{
			return E_ResponseErrorControl.BREAK;
		}
		
		switch( response.getError() )
		{
			case VERSION_MISMATCH:
			{
				if( !m_hasShownVersionMismatchDialog )
				{
					State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
					(
						"Version Mismatch",
						"There is a new version of b33hive available. Please refresh your browser."
					);
					
					queueAsyncDialog(State_AsyncDialog.class, constructor);
					
					m_hasShownVersionMismatchDialog = true;
				}
				
				return E_ResponseErrorControl.BREAK;
			}
			
			case NOT_AUTHORIZED:
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Not So Fast!",
					"You are not authorized to perform this action."
				);
				
				queueAsyncDialog(State_AsyncDialog.class, constructor);
				
				return E_ResponseErrorControl.BREAK;
			}
			
			case REDUNDANT:
			{
				return E_ResponseErrorControl.BREAK;
			}
			
			default:
			{
				if( !m_hasShownGeneralTransactionErrorDialog )
				{
					State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
					(
						"Oops!",
						"A connection error occurred...please try again later."
					);
					
					queueAsyncDialog(State_AsyncDialog.class, constructor);
					
					m_hasShownGeneralTransactionErrorDialog = true;
				}
				
				return E_ResponseErrorControl.BREAK;
			}
		}
	}
}
