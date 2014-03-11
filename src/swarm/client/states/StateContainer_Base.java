package swarm.client.states;

import swarm.client.app.AppContext;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;

import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateContainer;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

/**
 * ...
 * @author 
 */
public class StateContainer_Base extends A_StateContainer
{
	private final Class<? extends A_State> m_consoleState_T;
	
	public StateContainer_Base(Class<? extends A_State> consoleState_T)
	{
		m_consoleState_T = consoleState_T;
		registerAction(new Action_Base_HideSupplementState());
		registerAction(new Action_Base_ShowSupplementState());
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
	}
	
	@Override
	protected void willExit()
	{
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
		if ( revealingState == null )
		{
			if( m_consoleState_T != null )
			{
				if ( !this.isStateEntered(m_consoleState_T) )
				{
					enterState(this, m_consoleState_T);
					foregroundState(this, m_consoleState_T);
				}
			}
			
			if ( !this.isStateEntered(StateMachine_Camera.class) )
			{
				enterState(this, StateMachine_Camera.class);
				foregroundState(this, StateMachine_Camera.class);
			}
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
	}
}
