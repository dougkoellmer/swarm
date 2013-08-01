package com.b33hive.client.managers;

import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.structs.bhAccountInfo;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.client.transaction.bhE_ResponseErrorControl;
import com.b33hive.client.transaction.bhE_ResponseSuccessControl;
import com.b33hive.client.transaction.bhI_TransactionResponseHandler;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class bhGridManager implements bhI_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onGridResize();
	}
	
	private I_Listener m_listener = null;
	
	private static bhGridManager s_instance = null;

	public static void startUp()
	{
		s_instance = new bhGridManager();
	}
	
	public static bhGridManager getInstance()
	{
		return s_instance;
	}
	
	public void start(I_Listener listener)
	{
		m_listener = listener;
		
		bhClientTransactionManager.getInstance().addHandler(this);
	}
	
	public void stop()
	{
		m_listener = null;
		
		bhClientTransactionManager.getInstance().removeHandler(this);
	}
	
	public void getGridData(bhE_TransactionAction action)
	{
		bhClientTransactionManager.getInstance().performAction(action, bhE_RequestPath.getGridData);
	}
	
	void updateGridFromJson(bhI_JsonObject json)
	{
		bhClientGrid grid = bhClientGrid.getInstance();
		
		int oldSize = grid.getSize();
		
		grid.readJson(json);
		
		if( oldSize != grid.getSize() )
		{
			m_listener.onGridResize();
		}
	}

	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getGridData )
		{
			this.updateGridFromJson(response.getJson());
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.getUserData )
		{
			//--- DRK > A getUserData request can implicitly also create the user as well if this is the 
			//---		first getUserData (i.e., on signing up), or previous ones failed.  In turn, creating
			//---		a user can implicitly expand the grid.
			this.updateGridFromJson(response.getJson());
			
			return bhE_ResponseSuccessControl.CONTINUE;
		}
		
		return bhE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		//--- DRK > For now, bubbling all error up to StateMachine_Base so it shows error dialogs.
		
		if( request.getPath() == bhE_RequestPath.getGridData )
		{
			//return bhE_TransactionErrorControl.BREAK;
		}
		
		return bhE_ResponseErrorControl.CONTINUE;
	}
}
