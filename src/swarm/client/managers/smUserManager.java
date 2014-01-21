package swarm.client.managers;

import java.util.Iterator;

import swarm.client.app.smAppContext;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CellNuke;
import swarm.client.entities.smUserCell;
import swarm.client.managers.smClientAccountManager.E_ResponseType;
import swarm.client.structs.smAccountInfo;
import swarm.client.structs.smCellCodeCache;
import swarm.client.structs.smLocalCodeRepositoryWrapper;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_ResponseBatchListener;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smUserManager implements smI_TransactionResponseHandler, smClientAccountManager.I_Delegate, smI_ResponseBatchListener
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
	
	private final smLocalCodeRepositoryWrapper m_localCodeRepo = new smLocalCodeRepositoryWrapper();
	
	private final smA_ClientUser m_user;
	private final smAppContext m_appContext;
	
	public smUserManager(smAppContext context, smA_ClientUser user)
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
	
	public smA_ClientUser getUser()
	{
		return m_user;
	}
	
	public void getPosition(smE_TransactionAction action)
	{
		m_appContext.txnMngr.performAction(action, smE_RequestPath.getStartingPosition);
	}
	
	public void populateUser(smE_TransactionAction action)
	{
		m_appContext.txnMngr.performAction(action, smE_RequestPath.getUserData);
	}
	
	private void onGetUserDataSuccess(smTransactionResponse response)
	{
		smA_ClientUser user = m_user;
		user.readJson(m_appContext.jsonFactory, response.getJsonArgs());
		Boolean createdUser = m_appContext.jsonFactory.getHelper().getBoolean(response.getJsonArgs(), smE_JsonKey.createdUser);
		createdUser = createdUser != null ? createdUser : false; // can be null when reading inline transaction from the page and the user is already created.
		
		Iterator<? extends smUserCell> cellIterator = user.getCells();
		
		boolean firstIteration = true;
		
		while( cellIterator.hasNext() )
		{
			smUserCell userCell = cellIterator.next();
			
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
				smCellCodeManager codeManager = m_appContext.codeMngr;
				codeManager.nukeFromOrbit(userCell.getCoordinate(), smE_CellNuke.EVERYTHING);
				userCell.setCode(smE_CodeType.SOURCE, new smCode("", smE_CodeType.SOURCE));
				userCell.setCode(smE_CodeType.SPLASH, new smCode("", smE_CodeType.SPLASH));
				userCell.setCode(smE_CodeType.COMPILED, new smCode("", smE_CodeType.COMPILED));
				
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
				smCellAddressManager addressManager = m_appContext.addressMngr;
				addressManager.getCellAddress(userCell.getCoordinate(), smE_TransactionAction.MAKE_REQUEST);
			}
			else
			{
				for( int i = 0; i < smE_CodeType.values().length; i++ )
				{
					smE_CodeType ithType = smE_CodeType.values()[i];
					
					m_localCodeRepo.tryPopulatingCell(userCell.getCoordinate(), ithType, userCell);
				}
			}
		}
		
		m_listener.onUserPopulated();
	}

	@Override
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getStartingPosition )
		{
			smPoint startingPosition = new smPoint();
			startingPosition.readJson(m_appContext.jsonFactory, response.getJsonArgs());
			
			m_user.getLastPosition().copy(startingPosition);
			
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.getUserData )
		{
			this.onGetUserDataSuccess(response);
			
			return smE_ResponseSuccessControl.CONTINUE;
		}
		
		return smE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getStartingPosition )
		{
			//--- DRK > Trivial error, so just fail silently.
			m_user.getLastPosition().zeroOut();
			
			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.getUserData )
		{
			clearUser(); // shouldn't be necessary i think...but just in case
			
			if( response.getError() == smE_ResponseError.NOT_AUTHENTICATED )
			{
				if( m_failedToAuthenticateInBatch )
				{
					//--- DRK > We don't bubble this up to account manager because we "took a chance" by piggy-backing
					//---		the sign in/up request.  If it failed, this request couldn't succeed, but we don't let
					//---		the outside world know about the authentication error.
					//---		This case will be common either when signing up and trying common usernames, or when signing
					//---		in multiple times with a forgotten username/password combo.
					return smE_ResponseErrorControl.BREAK;
				}
			}
			else
			{
				if( m_authenticatedInBatch || m_appContext.accountMngr.isSignedIn() )
				{
					m_listener.onGetUserFailed();
					
					return smE_ResponseErrorControl.BREAK;
				}
			}
		}
		
		return smE_ResponseErrorControl.CONTINUE;
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
		smA_ClientUser user = m_user;
		
		if( user.isPopulated() )
		{
			m_appContext.txnMngr.cancelRequestsByPath(smE_RequestPath.getUserData);
			
			user.clearAllLocalChanges();
			
			Iterator<? extends smUserCell> cellIterator = user.getCells();
			
			smCellBuffer buffer = m_appContext.cellBufferMngr.getDisplayBuffer();
			
			while( cellIterator.hasNext() )
			{
				smUserCell userCell = cellIterator.next();
				smGridCoordinate coord = userCell.getCoordinate();
				
				if( buffer.getSubCellCount() == 1 )
				{
					if( buffer.isInBoundsAbsolute(coord) )
					{
						smBufferCell bufferCell = buffer.getCellAtAbsoluteCoord(coord);
						
						if( bufferCell == null )  continue;
						
						smCode sourceCode = userCell.getCode(smE_CodeType.SOURCE);
						
						if( sourceCode != null )
						{
							bufferCell.setCode(smE_CodeType.SOURCE, sourceCode);
						}
						
						if( bufferCell.isFocused() )
						{
							smCode compiledCode = userCell.getCode(smE_CodeType.COMPILED);
							
							if( compiledCode != null )
							{
								if( bufferCell.hasBeenPreviewed() )
								{
									//--- DRK > This makes sure that previews are cleared if the user is looking at previewed code
									//---		at their own cell and they sign out.
									bufferCell.setCode(smE_CodeType.COMPILED, compiledCode);
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
