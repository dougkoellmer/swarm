package b33hive.client.states;

import b33hive.client.app.bh_c;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;

import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_ActionArgs;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateContainer;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;



/**
 * ...
 * @author 
 */
public class StateContainer_Base extends bhA_StateContainer implements bhI_TransactionResponseHandler
{
	public static class HideSupplementState extends bhA_Action
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			container_backgroundState(this.getState(), StateMachine_Tabs.class);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return bhA_State.isForegrounded(StateMachine_Tabs.class);
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateContainer_Base.class;
		}
	}
	
	public static class ShowSupplementState extends bhA_Action 
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			container_foregroundState(this.getState(), StateMachine_Tabs.class);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return !bhA_State.isForegrounded(StateMachine_Tabs.class);
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateContainer_Base.class;
		}
	}
	
	public StateContainer_Base()
	{
		bhA_Action.register(new HideSupplementState());
		bhA_Action.register(new ShowSupplementState());
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		final bhClientTransactionManager transactionManager = bh_c.txnMngr;
		transactionManager.addHandler(this);
	}
	
	@Override
	protected void willExit()
	{
		final bhClientTransactionManager transactionManager = bh_c.txnMngr;
		transactionManager.removeHandler(this);
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		if ( revealingState == null )
		{
			if ( !this.isStateEntered(StateMachine_Tabs.class) )
			{
				container_enterState(this, StateMachine_Tabs.class);
				container_foregroundState(this, StateMachine_Tabs.class);
			}
			
			if ( !this.isStateEntered(StateMachine_Camera.class) )
			{
				container_enterState(this, StateMachine_Camera.class);
				container_foregroundState(this, StateMachine_Camera.class);
			}
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
		return bhE_ResponseErrorControl.CONTINUE;
	}
}
