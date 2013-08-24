package swarm.client.states;

import java.util.ArrayList;

import swarm.client.app.sm_c;
import swarm.client.entities.bhBufferCell;
import swarm.client.entities.bhA_ClientUser;
import swarm.client.managers.bhCellAddressManager;
import swarm.client.managers.bhCellBufferManager;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhGridManager;
import swarm.client.managers.bhUserManager;
import swarm.client.managers.bhClientAccountManager.E_ResponseType;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.transaction.bhE_ResponseErrorControl;
import swarm.client.transaction.bhE_ResponseSuccessControl;
import swarm.client.transaction.bhI_ResponseBatchListener;
import swarm.client.transaction.bhI_TransactionResponseHandler;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.shared.account.bhSignUpCredentials;
import swarm.shared.code.bhCompilerMessage;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.statemachine.bhA_Action;

import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_EventAction;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateMachine;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhE_TelemetryRequestPath;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


/**
 * ...
 * @author 
 */
public class StateMachine_Base extends bhA_StateMachine implements bhI_TransactionResponseHandler
{
	/*public static class PushDialog extends bhA_Action
	{
		@Override
		public void perform(Object[] args)
		{
			machine_pushState(this.getState(), State_GenericDialog.class, args[0]);
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}*/
	
	public static class OnAccountManagerResponse extends bhA_EventAction
	{
		public static class Args extends bhA_ActionArgs
		{
			private final bhClientAccountManager.E_ResponseType m_type;
			
			public Args(bhClientAccountManager.E_ResponseType type)
			{
				m_type = type;
			}
			
			public bhClientAccountManager.E_ResponseType getType()
			{
				return m_type;
			}
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnUserPopulated extends bhA_EventAction
	{
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnUserCleared extends bhA_EventAction
	{
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnGridResize extends bhA_EventAction
	{
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	private static class BatchListener implements bhI_ResponseBatchListener
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
	
	private static class AccountManagerDelegate implements bhClientAccountManager.I_Delegate
	{
		@Override
		public void onAccountTransactionResponse(E_ResponseType type)
		{
			OnAccountManagerResponse.Args args= new OnAccountManagerResponse.Args(type);
			bhA_Action.perform(OnAccountManagerResponse.class, args);
			
			StateMachine_Base baseMachine = bhA_State.getForegroundedInstance(StateMachine_Base.class);
			
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
			StateMachine_Base baseController = bhA_State.getEnteredInstance(StateMachine_Base.class);
			
			State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
			(
				"Sorry!",
				"Our system has lost track of you.  Please sign in again."
			);
			
			baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
		}
	}
	
	private final AccountManagerDelegate m_accountManagerDelegate = new AccountManagerDelegate();
	
	private final ArrayList<DialogData> m_asyncDialogQueue = new ArrayList<DialogData>();
	
	private boolean m_hasShownVersionMismatchDialog = false;
	private boolean m_hasShownGeneralTransactionErrorDialog = false;
	
	private final bhI_ResponseBatchListener m_batchListener = new BatchListener(this);
	
	void dequeueAsyncDialog()
	{
		m_asyncDialogQueue.remove(0);
		
		if( m_asyncDialogQueue.size() > 0 )
		{
			DialogData data = m_asyncDialogQueue.get(0);
			
			machine_setState(this, data.m_T__extends__State_AsyncDialog, data.m_constructor);
		}
		else
		{
			machine_popState(this);
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
			this.machine_pushState(this, T, constructor);
			
			m_asyncDialogQueue.add(null);
		}
		else
		{
			m_asyncDialogQueue.add(new DialogData(T, constructor));
		}
	}
	
	public StateMachine_Base()
	{
		//bhA_Action.register(new PushDialog());
		bhA_Action.register(new OnGridResize());
		bhA_Action.register(new OnAccountManagerResponse());
		
		bhA_Action.register(new OnUserPopulated());
		bhA_Action.register(new OnUserCleared());
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		final bhClientAccountManager accountManager = sm_c.accountMngr;
		final bhUserManager userManager = sm_c.userMngr;
		final bhGridManager gridManager = sm_c.gridMngr;
		final bhClientTransactionManager transactionManager = sm_c.txnMngr;
		
		transactionManager.addHandler(this);
		transactionManager.addBatchListener(m_batchListener);
		
		accountManager.start();
		
		userManager.start(new bhUserManager.I_Listener()
		{
			@Override
			public void onUserPopulated()
			{
				StateMachine_Base.this.performAction(OnUserPopulated.class);
			}

			@Override
			public void onUserDidClear()
			{
				StateMachine_Base.this.performAction(OnUserCleared.class);
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
		
		gridManager.start(new bhGridManager.I_Listener()
		{
			@Override
			public void onGridResize()
			{
				StateMachine_Base.this.performAction(StateMachine_Base.OnGridResize.class);
			}
		});
		
		accountManager.addDelegate(m_accountManagerDelegate);
	}
	
	@Override
	protected void willExit()
	{
		final bhClientAccountManager accountManager = sm_c.accountMngr;
		final bhUserManager userManager = sm_c.userMngr;
		final bhGridManager gridManager = sm_c.gridMngr;
		final bhClientTransactionManager transactionManager = sm_c.txnMngr;
		
		accountManager.removeDelegate(m_accountManagerDelegate);
		
		gridManager.stop();
		
		userManager.stop();
		
		accountManager.stop();
		
		transactionManager.removeBatchListener(m_batchListener);
		
		transactionManager.removeHandler(this);
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( this.getCurrentState() == null )
		{
			machine_setState(this, State_Initializing.class);
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
	}

	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		return bhE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() instanceof bhE_TelemetryRequestPath )
		{
			return bhE_ResponseErrorControl.BREAK;
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
						"There is a brand new version of b33hive available.  Please refresh your browser."
					);
					
					queueAsyncDialog(State_AsyncDialog.class, constructor);
					
					m_hasShownVersionMismatchDialog = true;
				}
				
				return bhE_ResponseErrorControl.BREAK;
			}
			
			case NOT_AUTHORIZED:
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Not So Fast!",
					"You are not authorized to perform this action."
				);
				
				queueAsyncDialog(State_AsyncDialog.class, constructor);
				
				return bhE_ResponseErrorControl.BREAK;
			}
			
			case REDUNDANT:
			{
				return bhE_ResponseErrorControl.BREAK;
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
				
				return bhE_ResponseErrorControl.BREAK;
			}
		}
	}
}
