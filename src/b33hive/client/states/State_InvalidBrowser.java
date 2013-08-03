package b33hive.client.states;

import b33hive.client.entities.bhClientGrid;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhGridManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.transaction.bhInlineRequestDispatcher;

import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
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


