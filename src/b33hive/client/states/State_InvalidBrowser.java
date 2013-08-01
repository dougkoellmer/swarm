package com.b33hive.client.states;

import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.entities.bhClientUser;
import com.b33hive.client.managers.bhClientAccountManager;
import com.b33hive.client.managers.bhGridManager;
import com.b33hive.client.managers.bhUserManager;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.client.transaction.bhE_ResponseErrorControl;
import com.b33hive.client.transaction.bhE_ResponseSuccessControl;
import com.b33hive.client.transaction.bhGwtRequestDispatcher;
import com.b33hive.client.transaction.bhI_TransactionResponseHandler;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.client.transaction.bhInlineRequestDispatcher;

import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
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


