package swarm.client.states;

import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smInlineRequestDispatcher;

import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

public class State_InvalidBrowser extends smA_State
{
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
	}
	
	@Override
	protected void update(double timeStep)
	{
		
	}
	
	@Override
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
	}
}


