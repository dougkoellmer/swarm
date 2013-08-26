package swarm.client.states;

import java.util.ArrayList;

import swarm.client.app.sm_c;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellBufferManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.managers.smClientAccountManager.E_ResponseType;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_ResponseBatchListener;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.code.smCompilerMessage;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_EventAction;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smE_TelemetryRequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


/**
 * ...
 * @author 
 */
public class StateMachine_Base extends smA_StateMachine implements smI_TransactionResponseHandler
{
	/*public static class PushDialog extends smA_Action
	{
		@Override
		public void perform(Object[] args)
		{
			machine_pushState(this.getState(), State_GenericDialog.class, args[0]);
		}

		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}*/
	
	public static class OnAccountManagerResponse extends smA_EventAction
	{
		public static class Args extends smA_ActionArgs
		{
			private final smClientAccountManager.E_ResponseType m_type;
			
			public Args(smClientAccountManager.E_ResponseType type)
			{
				m_type = type;
			}
			
			public smClientAccountManager.E_ResponseType getType()
			{
				return m_type;
			}
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnUserPopulated extends smA_EventAction
	{
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnUserCleared extends smA_EventAction
	{
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	public static class OnGridResize extends smA_EventAction
	{
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateMachine_Base.class;
		}
	}
	
	private static class BatchListener implements smI_ResponseBatchListener
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
	
	private static class AccountManagerDelegate implements smClientAccountManager.I_Delegate
	{
		@Override
		public void onAccountTransactionResponse(E_ResponseType type)
		{
			OnAccountManagerResponse.Args args= new OnAccountManagerResponse.Args(type);
			smA_Action.perform(OnAccountManagerResponse.class, args);
			
			StateMachine_Base baseMachine = smA_State.getForegroundedInstance(StateMachine_Base.class);
			
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
			StateMachine_Base baseController = smA_State.getEnteredInstance(StateMachine_Base.class);
			
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
	
	private final smI_ResponseBatchListener m_batchListener = new BatchListener(this);
	
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
		//smA_Action.register(new PushDialog());
		smA_Action.register(new OnGridResize());
		smA_Action.register(new OnAccountManagerResponse());
		
		smA_Action.register(new OnUserPopulated());
		smA_Action.register(new OnUserCleared());
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		final smClientAccountManager accountManager = sm_c.accountMngr;
		final smUserManager userManager = sm_c.userMngr;
		final smGridManager gridManager = sm_c.gridMngr;
		final smClientTransactionManager transactionManager = sm_c.txnMngr;
		
		transactionManager.addHandler(this);
		transactionManager.addBatchListener(m_batchListener);
		
		accountManager.start();
		
		userManager.start(new smUserManager.I_Listener()
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
		
		gridManager.start(new smGridManager.I_Listener()
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
		final smClientAccountManager accountManager = sm_c.accountMngr;
		final smUserManager userManager = sm_c.userMngr;
		final smGridManager gridManager = sm_c.gridMngr;
		final smClientTransactionManager transactionManager = sm_c.txnMngr;
		
		accountManager.removeDelegate(m_accountManagerDelegate);
		
		gridManager.stop();
		
		userManager.stop();
		
		accountManager.stop();
		
		transactionManager.removeBatchListener(m_batchListener);
		
		transactionManager.removeHandler(this);
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
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
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		return smE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() instanceof smE_TelemetryRequestPath )
		{
			return smE_ResponseErrorControl.BREAK;
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
				
				return smE_ResponseErrorControl.BREAK;
			}
			
			case NOT_AUTHORIZED:
			{
				State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor
				(
					"Not So Fast!",
					"You are not authorized to perform this action."
				);
				
				queueAsyncDialog(State_AsyncDialog.class, constructor);
				
				return smE_ResponseErrorControl.BREAK;
			}
			
			case REDUNDANT:
			{
				return smE_ResponseErrorControl.BREAK;
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
				
				return smE_ResponseErrorControl.BREAK;
			}
		}
	}
}
