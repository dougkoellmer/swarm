package swarm.client.states.account;

import swarm.client.app.sm_c;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smClientAccountManager.E_ResponseType;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smA_StateConstructor;



/**
 * ...
 * @author 
 */
public class StateMachine_Account extends smA_StateMachine
{
	private static class AccountManagerDelegate implements bhClientAccountManager.I_Delegate
	{
		AccountManagerDelegate()
		{
		}

		@Override
		public void onAccountTransactionResponse(E_ResponseType type)
		{
			onAccountManagerDelegation();
		}

		@Override
		public void onAuthenticationError()
		{
			bhClientAccountManager accountManager = sm_c.accountMngr;
			
			//--- DRK > Not entering this if block should be an extremely fringe, practically impossible case.
			//---		It's still *technically* possible though, so we don't want to clear the spinner if we're still
			//---		waiting on a response from the server
			if( !accountManager.isWaitingOnServer() )
			{
				onAccountManagerDelegation();
			}
		}
		
		private void onAccountManagerDelegation()
		{
			StateMachine_Account accountMachine = smA_State.getForegroundedInstance(StateMachine_Account.class);

			if( accountMachine != null )
			{
				accountMachine.popBlockerAndSetState();
			}
			else
			{
				bhU_Debug.ASSERT(false);
			}
		}
	}
	
	private final AccountManagerDelegate m_accountManagerDelegate = new AccountManagerDelegate();
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] args)
	{
		bhClientAccountManager accountManager = sm_c.accountMngr;
		
		accountManager.addDelegate(m_accountManagerDelegate);
		
		if( accountManager.isWaitingOnServer() )
		{
			if( this.getCurrentState() == null )
			{
				if( accountManager.isSignedIn() )
				{
					machine_setState(this, State_ManageAccount.class);
				}
				else
				{
					machine_setState(this, State_SignInOrUp.class);
				}
			}
			
			if( !(this.getCurrentState() instanceof State_AccountStatusPending) )
			{
				machine_pushState(this, State_AccountStatusPending.class);
			}
		}
		else
		{
			popBlockerAndSetState();
		}
	}
	
	void popBlockerAndSetState()
	{
		bhU_Debug.ASSERT(this.isForegrounded(), "popBlockerAndSetState1");
		
		bhClientAccountManager accountManager = sm_c.accountMngr;
		
		machine_beginBatch(this);
	
		if( (this.getCurrentState() instanceof State_AccountStatusPending) )
		{
			machine_popState(this);
		}
		
		smA_State currentState = this.getCurrentState();
		
		if( accountManager.isSignedIn() )
		{
			if( currentState == null || currentState instanceof State_SignInOrUp )
			{
				machine_setState(this, State_ManageAccount.class);
			}
		}
		else
		{
			if( currentState == null || currentState instanceof State_ManageAccount )
			{
				machine_setState(this, State_SignInOrUp.class);
			}
		}
		
		machine_endBatch(this);
	}
	
	@Override 
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		bhClientAccountManager accountManager = sm_c.accountMngr;
		
		accountManager.removeDelegate(m_accountManagerDelegate);
		
		if( blockingState == null )
		{
			if( this.getCurrentState() instanceof State_AccountStatusPending )
			{
				machine_popState(this);
			}
		}
	}
}