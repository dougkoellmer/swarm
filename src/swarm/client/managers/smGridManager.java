package swarm.client.managers;

import swarm.client.app.smAppContext;
import swarm.client.states.StateMachine_Base;
import swarm.client.structs.smAccountInfo;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.entities.smA_Grid;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class smGridManager implements smI_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onGridResize();
	}
	
	private I_Listener m_listener = null;
	private final smA_Grid m_grid;
	private final smClientTransactionManager m_txnMngr;
	private final smA_JsonFactory m_jsonFactory;
	
	public smGridManager(smClientTransactionManager txnMngr, smA_JsonFactory jsonFactory, smA_Grid grid)
	{
		m_jsonFactory = jsonFactory;
		m_txnMngr = txnMngr;
		m_grid = grid;
	}
	
	public smA_Grid getGrid()
	{
		return m_grid;
	}
	
	public void start(I_Listener listener)
	{
		m_listener = listener;
		
		m_txnMngr.addHandler(this);
	}
	
	public void stop()
	{
		m_listener = null;
		
		m_txnMngr.removeHandler(this);
	}
	
	public void getGridData(smE_TransactionAction action)
	{
		m_txnMngr.performAction(action, smE_RequestPath.getGridData);
	}
	
	void updateGridFromJson(smI_JsonObject json)
	{
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		m_grid.readJson(m_jsonFactory, json);
		
		if( oldWidth != m_grid.getWidth() || oldHeight != m_grid.getHeight() )
		{
			m_listener.onGridResize();
		}
	}

	@Override
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getGridData )
		{
			this.updateGridFromJson(response.getJsonArgs());
			
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.getUserData )
		{
			//--- DRK > A getUserData request can implicitly also create the user as well if this is the 
			//---		first getUserData (i.e., on signing up), or previous ones failed.  In turn, creating
			//---		a user can implicitly expand the grid.
			this.updateGridFromJson(response.getJsonArgs());
			
			return smE_ResponseSuccessControl.CONTINUE;
		}
		
		return smE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		//--- DRK > For now, bubbling all error up to StateMachine_Base so it shows error dialogs.
		
		if( request.getPath() == smE_RequestPath.getGridData )
		{
			//return smE_TransactionErrorControl.BREAK;
		}
		
		return smE_ResponseErrorControl.CONTINUE;
	}
}
