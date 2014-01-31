package swarm.client.states;

import swarm.client.entities.A_ClientUser;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.InlineRequestDispatcher;

import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_InvalidBrowser extends A_State
{
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void update(double timeStep)
	{
		
	}
	
	@Override
	protected void willBackground(Class<? extends A_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
	}
}


