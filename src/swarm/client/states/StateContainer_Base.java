package swarm.client.states;

import swarm.client.app.smAppContext;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;

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
	private final Class<? extends smA_State> m_consoleState_T;
	
	public StateContainer_Base(Class<? extends smA_State> consoleState_T)
	{
		m_consoleState_T = consoleState_T;
		registerAction(new Action_Base_HideSupplementState());
		registerAction(new Action_Base_ShowSupplementState());
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
			if( m_consoleState_T != null )
			{
				if ( !this.isStateEntered(m_consoleState_T) )
				{
					container_enterState(this, m_consoleState_T);
					container_foregroundState(this, m_consoleState_T);
				}
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
