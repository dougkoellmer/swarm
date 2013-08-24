package swarm.client.states;

import swarm.client.entities.bhA_ClientUser;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhGridManager;
import swarm.client.managers.bhUserManager;
import swarm.client.transaction.bhE_TransactionAction;
import swarm.client.transaction.bhE_ResponseErrorControl;
import swarm.client.transaction.bhE_ResponseSuccessControl;
import swarm.client.transaction.bhI_TransactionResponseHandler;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.client.transaction.bhInlineRequestDispatcher;

import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_InvalidBrowser extends bhA_State
{
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void update(double timeStep)
	{
		
	}
	
	@Override
	protected void willBackground(Class<? extends bhA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
	}
}


