package swarm.client.states.account;

import swarm.client.app.AppContext;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.ClientAccountManager.E_ResponseType;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.A_StateConstructor;



/**
 * ...
 * @author 
 */
public class StateMachine_Account extends A_StateMachine
{
	private static class AccountManagerDelegate implements ClientAccountManager.I_Delegate
	{
		private final ClientAccountManager m_accountMngr;
		private final StateMachine_Account m_state;
		
		AccountManagerDelegate(StateMachine_Account state, ClientAccountManager accountManager)
		{
			m_state = state;
			m_accountMngr = accountManager;
		}

		@Override
		public void onAccountTransactionResponse(E_ResponseType type)
		{
			onAccountManagerDelegation();
		}

		@Override
		public void onAuthenticationError()
		{
			//--- DRK > Not entering this if block should be an extremely fringe, practically impossible case.
			//---		It's still *technically* possible though, so we don't want to clear the spinner if we're still
			//---		waiting on a response from the server
			if( !m_accountMngr.isWaitingOnServer() )
			{
				onAccountManagerDelegation();
			}
		}
		
		private void onAccountManagerDelegation()
		{
			StateMachine_Account accountMachine = m_state.getContext().getForegroundedState(StateMachine_Account.class);

			if( accountMachine != null )
			{
				accountMachine.popBlockerAndSetState();
			}
			else
			{
				U_Debug.ASSERT(false);
			}
		}
	}
	
	private final AccountManagerDelegate m_accountManagerDelegate;
	private final ClientAccountManager m_accountMngr;
	
	public StateMachine_Account(ClientAccountManager accountMngr)
	{
		m_accountManagerDelegate = new AccountManagerDelegate(this, accountMngr);
		m_accountMngr = accountMngr;
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] args)
	{
		ClientAccountManager accountManager = m_accountMngr;
		
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
		U_Debug.ASSERT(this.isForegrounded(), "popBlockerAndSetState1");
		
		ClientAccountManager accountManager = m_accountMngr;
		
		machine_beginBatch(this);
	
		if( (this.getCurrentState() instanceof State_AccountStatusPending) )
		{
			machine_popState(this);
		}
		
		A_State currentState = this.getCurrentState();
		
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
	protected void willBackground(Class<? extends A_State> blockingState)
	{
		ClientAccountManager accountManager = m_accountMngr;
		
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