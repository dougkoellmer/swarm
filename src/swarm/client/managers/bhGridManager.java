package swarm.client.managers;

import swarm.client.app.sm_c;
import swarm.client.states.StateMachine_Base;
import swarm.client.structs.bhAccountInfo;
import swarm.client.transaction.bhE_TransactionAction;
import swarm.client.transaction.bhE_ResponseErrorControl;
import swarm.client.transaction.bhE_ResponseSuccessControl;
import swarm.client.transaction.bhI_TransactionResponseHandler;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.shared.entities.bhA_Grid;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class bhGridManager implements bhI_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onGridResize();
	}
	
	private I_Listener m_listener = null;
	private final bhA_Grid m_grid;
	
	public bhGridManager(bhA_Grid grid)
	{
		m_grid = grid;
	}
	
	public bhA_Grid getGrid()
	{
		return m_grid;
	}
	
	public void start(I_Listener listener)
	{
		m_listener = listener;
		
		sm_c.txnMngr.addHandler(this);
	}
	
	public void stop()
	{
		m_listener = null;
		
		sm_c.txnMngr.removeHandler(this);
	}
	
	public void getGridData(bhE_TransactionAction action)
	{
		sm_c.txnMngr.performAction(action, bhE_RequestPath.getGridData);
	}
	
	void updateGridFromJson(bhI_JsonObject json)
	{
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		m_grid.readJson(json);
		
		if( oldWidth != m_grid.getWidth() || oldHeight != m_grid.getHeight() )
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
