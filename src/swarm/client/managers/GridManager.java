package swarm.client.managers;

import swarm.client.app.AppContext;
import swarm.client.states.StateMachine_Base;
import swarm.client.structs.AccountInfo;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.A_User;
import swarm.shared.entities.U_User;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.lang.Boolean;
import swarm.shared.reflection.I_Callback;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class GridManager implements I_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onGridUpdate();
	}
	
	private static final CellAddressMapping s_utilMapping1 = new CellAddressMapping();
	private static final Boolean s_utilBool = new Boolean();
	
	private I_Listener m_listener = null;
	private final A_Grid m_grid;
	private final ClientTransactionManager m_txnMngr;
	private final A_JsonFactory m_jsonFactory;
	
	public GridManager(ClientTransactionManager txnMngr, A_JsonFactory jsonFactory, A_Grid grid)
	{
		m_jsonFactory = jsonFactory;
		m_txnMngr = txnMngr;
		m_grid = grid;
	}
	
	public A_Grid getGrid()
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
	
	public void getGridData(E_TransactionAction action)
	{
		m_txnMngr.performAction(action, E_RequestPath.getGridData);
	}
	
	private boolean updateGridSizeFromJson(I_JsonObject json)
	{
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		m_grid.readJson(json, m_jsonFactory);
		
		if( oldWidth != m_grid.getWidth() || oldHeight != m_grid.getHeight() )
		{
			return true;
		}
		
		return false;
	}
	
	private boolean markTakenCells(I_JsonObject json)
	{
		s_utilBool.value = false;
		
		I_Callback callback = new I_Callback()
		{			
			@Override
			public void invoke(Object... args)
			{
				if( !m_grid.isTaken(s_utilMapping1.getCoordinate()) )
				{
					m_grid.claimCoordinate(s_utilMapping1.getCoordinate());
					s_utilBool.value = true;
				}
			}
		};
		
		U_User.readUserCells(m_jsonFactory, json, s_utilMapping1, callback);
		
		return s_utilBool.value;
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getGridData )
		{
			if( this.updateGridSizeFromJson(response.getJsonArgs()))
			{
				m_listener.onGridUpdate();
			}
			
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.getUserData )
		{
			//--- DRK > A getUserData request can implicitly also create the user as well if this is the 
			//---		first getUserData (i.e., on signing up), or previous ones failed.  In turn, creating
			//---		a user can implicitly expand the grid.
			boolean resized = this.updateGridSizeFromJson(response.getJsonArgs());
			
			//--- DRK > The getUserData request may have actually made the user, which could create a new cell,
			//--		so we just be safe here and mark all the user's cells as taken, even though they might be already.
			boolean newCellTaken = this.markTakenCells(response.getJsonArgs());
			
			if( resized || newCellTaken )
			{
				m_listener.onGridUpdate();
			}
			
			return E_ResponseSuccessControl.CONTINUE;
		}
		
		return E_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		//--- DRK > For now, bubbling all error up to StateMachine_Base so it shows error dialogs.
		
		if( request.getPath() == E_RequestPath.getGridData )
		{
			//return smE_TransactionErrorControl.BREAK;
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}
}
