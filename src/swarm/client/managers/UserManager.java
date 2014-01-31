package swarm.client.managers;

import java.util.Iterator;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.E_CellNuke;
import swarm.client.entities.UserCell;
import swarm.client.managers.ClientAccountManager.E_ResponseType;
import swarm.client.structs.AccountInfo;
import swarm.client.structs.CellCodeCache;
import swarm.client.structs.LocalCodeRepositoryWrapper;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_ResponseBatchListener;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class UserManager implements I_TransactionResponseHandler, ClientAccountManager.I_Delegate, I_ResponseBatchListener
{
	public static interface I_Listener
	{
		void onGetUserFailed();
		
		void onUserPopulated();
		
		void onUserDidClear();
	}
	
	private boolean m_failedToAuthenticateInBatch = false;
	private boolean m_authenticatedInBatch = false;
	
	I_Listener m_listener = null;
	
	private final LocalCodeRepositoryWrapper m_localCodeRepo = new LocalCodeRepositoryWrapper();
	
	private final A_ClientUser m_user;
	private final AppContext m_appContext;
	
	public UserManager(AppContext context, A_ClientUser user)
	{
		m_appContext = context;
		m_user = user;
	}
	
	public void start(I_Listener listener)
	{
		m_localCodeRepo.addSource(m_appContext.cellBufferMngr);
		m_localCodeRepo.addSource(m_appContext.codeCache);
		
		m_appContext.txnMngr.addHandler(this);
		m_appContext.txnMngr.addBatchListener(this);
		m_appContext.accountMngr.addDelegate(this);
		
		m_listener = listener;
	}
	
	public void stop()
	{
		m_listener = null;
		
		m_appContext.accountMngr.removeDelegate(this);
		m_appContext.txnMngr.removeBatchListener(this);
		m_appContext.txnMngr.removeHandler(this);
		
		m_localCodeRepo.removeAllSources();
	}
	
	public A_ClientUser getUser()
	{
		return m_user;
	}
	
	public void getPosition(E_TransactionAction action)
	{
		m_appContext.txnMngr.performAction(action, E_RequestPath.getStartingPosition);
	}
	
	public void populateUser(E_TransactionAction action)
	{
		m_appContext.txnMngr.performAction(action, E_RequestPath.getUserData);
	}
	
	private void onGetUserDataSuccess(TransactionResponse response)
	{
		A_ClientUser user = m_user;
		user.readJson(m_appContext.jsonFactory, response.getJsonArgs());
		Boolean createdUser = m_appContext.jsonFactory.getHelper().getBoolean(response.getJsonArgs(), E_JsonKey.createdUser);
		createdUser = createdUser != null ? createdUser : false; // can be null when reading inline transaction from the page and the user is already created.
		
		Iterator<? extends UserCell> cellIterator = user.getCells();
		
		boolean firstIteration = true;
		
		while( cellIterator.hasNext() )
		{
			UserCell userCell = cellIterator.next();
			
			//--- DRK > NOTE:	This block used to kinda make sense with b33hive, but now with more generic usage
			//---				like dougkoellmer.com, a user's cell doesn't necessarily have the username in it.
			/*if( firstIteration )
			{
				smAccountInfo info = m_appContext.accountMngr.getAccountInfo();
				userCell.setAddress(new smCellAddress(info.get(smAccountInfo.Type.USERNAME)));
				
				firstIteration = false;
			}*/
			
			//TODO: When user can have more than one cell, try to populate other cell addresses from local sources.
			
			if( createdUser )
			{
				//--- DRK > Cell could have some code in it, like the "empty-cell" code, so we want to
				//---		make sure everything's house-cleaned before the user moves in.
				//---		Note that this code will have to be placed elsewhere if/when cells are taken
				//---		manually in the future, and not implicitly when creating the user.
				CellCodeManager codeManager = m_appContext.codeMngr;
				codeManager.nukeFromOrbit(userCell.getCoordinate(), E_CellNuke.EVERYTHING);
				userCell.setCode(E_CodeType.SOURCE, new Code("", E_CodeType.SOURCE));
				userCell.setCode(E_CodeType.SPLASH, new Code("", E_CodeType.SPLASH));
				userCell.setCode(E_CodeType.COMPILED, new Code("", E_CodeType.COMPILED));
				
				//--- DRK > This block is now handled implicitly by the grid manager.
				//---		Grid manager listens for this request success, and updates
				//---		the grid if a cell is created, firing an event that eventually
				//---		causes the cell buffer to update, which fills the empty buffer cell
				//---		with the code in the user cell created above...phew.
				/*smCellBuffer buffer = m_appContext.cellBufferMngr.getDisplayBuffer();
				smGridCoordinate coord = userCell.getCoordinate();
				
				if( buffer.getSubCellCount() == 1 )
				{
					if( buffer.isInBoundsAbsolute(coord) )
					{
						smBufferCell bufferCell = buffer.getCellAtAbsoluteCoord(coord);
						bufferCell.copy(userCell);
					}
				}*/
				
				//--- DRK > This will just loop back into the user to find the cell address that we now know,
				//---		and then instantly let the View know about it. No transaction should go out.
				//---		The View cares about this in one situation...when the user is focused on the cell that
				//---		they just took ownership of...this is/should-be impossible with e.g. b33hive, where the
				//---		cells don't appear until they are actually owned.
				CellAddressManager addressManager = m_appContext.addressMngr;
				addressManager.getCellAddress(userCell.getCoordinate(), E_TransactionAction.MAKE_REQUEST);
			}
			else
			{
				for( int i = 0; i < E_CodeType.values().length; i++ )
				{
					E_CodeType ithType = E_CodeType.values()[i];
					
					m_localCodeRepo.tryPopulatingCell(userCell.getCoordinate(), ithType, userCell);
				}
			}
		}
		
		m_listener.onUserPopulated();
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getStartingPosition )
		{
			Point startingPosition = new Point();
			startingPosition.readJson(m_appContext.jsonFactory, response.getJsonArgs());
			
			m_user.getLastPosition().copy(startingPosition);
			
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.getUserData )
		{
			this.onGetUserDataSuccess(response);
			
			return E_ResponseSuccessControl.CONTINUE;
		}
		
		return E_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getStartingPosition )
		{
			//--- DRK > Trivial error, so just fail silently.
			m_user.getLastPosition().zeroOut();
			
			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.getUserData )
		{
			clearUser(); // shouldn't be necessary i think...but just in case
			
			if( response.getError() == E_ResponseError.NOT_AUTHENTICATED )
			{
				if( m_failedToAuthenticateInBatch )
				{
					//--- DRK > We don't bubble this up to account manager because we "took a chance" by piggy-backing
					//---		the sign in/up request.  If it failed, this request couldn't succeed, but we don't let
					//---		the outside world know about the authentication error.
					//---		This case will be common either when signing up and trying common usernames, or when signing
					//---		in multiple times with a forgotten username/password combo.
					return E_ResponseErrorControl.BREAK;
				}
			}
			else
			{
				if( m_authenticatedInBatch || m_appContext.accountMngr.isSignedIn() )
				{
					m_listener.onGetUserFailed();
					
					return E_ResponseErrorControl.BREAK;
				}
			}
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}

	@Override
	public void onAccountTransactionResponse(E_ResponseType type)
	{
		switch(type)
		{
			case SIGN_OUT_SUCCESS:
			{
				clearUser();
				
				break;
			}
			
			case SIGN_IN_SUCCESS:
			case SIGN_UP_SUCCESS:
			case PASSWORD_CONFIRM_SUCCESS:
			{
				if( m_appContext.txnMngr.isInBatch() )
				{
					m_authenticatedInBatch = true;
				}
				
				break;
			}
			
			case SIGN_IN_FAILURE:
			case SIGN_UP_FAILURE:
			case PASSWORD_CONFIRM_FAILURE:
			{
				if( m_appContext.txnMngr.isInBatch() )
				{
					m_failedToAuthenticateInBatch = true;
				}
				
				break;
			}
		}
	}
	
	private void clearUser()
	{
		A_ClientUser user = m_user;
		
		if( user.isPopulated() )
		{
			m_appContext.txnMngr.cancelRequestsByPath(E_RequestPath.getUserData);
			
			user.clearAllLocalChanges();
			
			Iterator<? extends UserCell> cellIterator = user.getCells();
			
			CellBuffer buffer = m_appContext.cellBufferMngr.getDisplayBuffer();
			
			while( cellIterator.hasNext() )
			{
				UserCell userCell = cellIterator.next();
				GridCoordinate coord = userCell.getCoordinate();
				
				if( buffer.getSubCellCount() == 1 )
				{
					if( buffer.isInBoundsAbsolute(coord) )
					{
						BufferCell bufferCell = buffer.getCellAtAbsoluteCoord(coord);
						
						if( bufferCell == null )  continue;
						
						Code sourceCode = userCell.getCode(E_CodeType.SOURCE);
						
						if( sourceCode != null )
						{
							bufferCell.setCode(E_CodeType.SOURCE, sourceCode);
						}
						
						if( bufferCell.isFocused() )
						{
							Code compiledCode = userCell.getCode(E_CodeType.COMPILED);
							
							if( compiledCode != null )
							{
								if( bufferCell.hasBeenPreviewed() )
								{
									//--- DRK > This makes sure that previews are cleared if the user is looking at previewed code
									//---		at their own cell and they sign out.
									bufferCell.setCode(E_CodeType.COMPILED, compiledCode);
								}
							}
						}
					}
				}
				
				//--- DRK > Just dumping all we can into other local code repositories
				//---		because it won't be available in user object anymore.
				m_appContext.codeCache.cacheCell(userCell);
			}
			
			user.onSignOut();
			
			m_listener.onUserDidClear();
		}
	}

	@Override
	public void onAuthenticationError()
	{
		clearUser();
	}

	@Override
	public void onResponseBatchStart()
	{
		m_failedToAuthenticateInBatch = false;
		m_authenticatedInBatch = false;
	}
	
	@Override
	public void onResponseBatchEnd()
	{
	}
}
