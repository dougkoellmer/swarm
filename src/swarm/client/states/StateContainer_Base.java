package swarm.client.states;

import swarm.client.app.smAppContext;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateContainer;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

/**
 * ...
 * @author 
 */
public class StateContainer_Base extends smA_StateContainer
{
	public static class HideSupplementState extends smA_Action
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			container_backgroundState(this.getState(), StateMachine_Tabs.class);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			return smA_State.isForegrounded(StateMachine_Tabs.class);
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateContainer_Base.class;
		}
	}
	
	public static class ShowSupplementState extends smA_Action 
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			container_foregroundState(this.getState(), StateMachine_Tabs.class);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			return !smA_State.isForegrounded(StateMachine_Tabs.class);
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return StateContainer_Base.class;
		}
	}
	
	public StateContainer_Base()
	{
		smA_Action.register(new HideSupplementState());
		smA_Action.register(new ShowSupplementState());
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void willExit()
	{
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
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
}
