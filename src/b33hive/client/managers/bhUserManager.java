package b33hive.client.managers;

import java.util.Iterator;

import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhCamera;
import b33hive.client.entities.bhClientGrid;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.entities.bhE_CellNuke;
import b33hive.client.entities.bhUserCell;
import b33hive.client.managers.bhClientAccountManager.E_ResponseType;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.structs.bhAccountInfo;
import b33hive.client.structs.bhCellCodeCache;
import b33hive.client.structs.bhI_LocalCodeRepository;
import b33hive.client.structs.bhLocalCodeRepositoryWrapper;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhI_ResponseBatchListener;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import b33hive.shared.utils.bhU_Singletons;

public class bhUserManager implements bhI_TransactionResponseHandler, bhClientAccountManager.I_Delegate, bhI_ResponseBatchListener
{
	public static interface I_Listener
	{
		void onGetUserFailed();
		
		void onUserPopulated();
		
		void onUserDidClear();
	}
	
	private boolean m_failedToAuthenticateInBatch = false;
	private boolean m_authenticatedInBatch = false;
	private final bhClientAccountManager m_accountManager;
	
	I_Listener m_listener = null;
	
	private final bhLocalCodeRepositoryWrapper m_localCodeRepo = new bhLocalCodeRepositoryWrapper();
	
	private final bhA_ClientUser m_user;
	
	public bhUserManager(bhClientAccountManager accountManager, bhA_ClientUser user)
	{
		m_accountManager = accountManager;
		m_user = user;
		
		m_localCodeRepo.addSource(bhCellBufferManager.getInstance());
		m_localCodeRepo.addSource(bhCellCodeCache.getInstance());
	}
	
	public void start(I_Listener listener)
	{
		bhClientTransactionManager.getInstance().addHandler(this);
		bhClientTransactionManager.getInstance().addBatchListener(this);
		m_accountManager.addDelegate(this);
		
		m_listener = listener;
	}
	
	public void stop()
	{
		m_listener = null;
		
		m_accountManager.removeDelegate(this);
		bhClientTransactionManager.getInstance().removeBatchListener(this);
		bhClientTransactionManager.getInstance().removeHandler(this);
	}
	
	public bhA_ClientUser getUser()
	{
		return m_user;
	}
	
	public void getPosition(bhE_TransactionAction action)
	{
		bhClientTransactionManager.getInstance().performAction(action, bhE_RequestPath.getStartingPosition);
	}
	
	public void populateUser(bhE_TransactionAction action)
	{
		bhClientTransactionManager.getInstance().performAction(action, bhE_RequestPath.getUserData);
	}
	
	private void onGetUserDataSuccess(bhTransactionResponse response)
	{
		bhA_ClientUser user = m_user;
		user.readJson(response.getJson());
		Boolean createdUser = bhJsonHelper.getInstance().getBoolean(response.getJson(), bhE_JsonKey.createdUser);
		createdUser = createdUser != null ? createdUser : false; // can be null when reading inline transaction from the page and the user is already created.
		
		Iterator<? extends bhUserCell> cellIterator = user.getCells();
		
		boolean firstIteration = true;
		
		while( cellIterator.hasNext() )
		{
			bhUserCell userCell = cellIterator.next();
			
			if( firstIteration )
			{
				bhAccountInfo info = m_accountManager.getAccountInfo();
				userCell.setAddress(new bhCellAddress(info.get(bhAccountInfo.Type.USERNAME)));
				
				firstIteration = false;
			}
			
			//TODO: When user can have more than one cell, try to populate other cell addresses from local sources.
			
			if( createdUser )
			{
				//--- DRK > Cell could have some code in it, like the "empty-cell" code, so we want to
				//---		make sure everything's house-cleaned before the user moves in.
				//---		Note that this code will have to be placed elsewhere if/when cells are taken
				//---		manually in the future, and not implicitly when creating the user.
				bhCellCodeManager codeManager = bhU_Singletons.get(bhCellCodeManager.class);
				codeManager.nukeFromOrbit(userCell.getCoordinate(), bhE_CellNuke.EVERYTHING);
				userCell.setCode(bhE_CodeType.SOURCE, new bhCode("", bhE_CodeType.SOURCE));
				userCell.setCode(bhE_CodeType.SPLASH, new bhCode("", bhE_CodeType.SPLASH));
				userCell.setCode(bhE_CodeType.COMPILED, new bhCode("", bhE_CodeType.COMPILED));
				
				bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
				bhGridCoordinate coord = userCell.getCoordinate();
				
				if( buffer.getCellSize() == 1 )
				{
					if( buffer.isInBoundsAbsolute(coord) )
					{
						bhBufferCell bufferCell = buffer.getCellAtAbsoluteCoord(coord);
						bufferCell.copy(userCell);
					}
				}
				
				//--- DRK > This will just loop back into the user to find the cell address that we now know,
				//---		and then instantly let the View know about it. No transaction should go out.
				bhCellAddressManager addressManager = bhU_Singletons.get(bhCellAddressManager.class);
				addressManager.getCellAddress(userCell.getCoordinate(), bhE_TransactionAction.MAKE_REQUEST);
			}
			else
			{
				for( int i = 0; i < bhE_CodeType.values().length; i++ )
				{
					bhE_CodeType ithType = bhE_CodeType.values()[i];
					
					m_localCodeRepo.tryPopulatingCell(userCell.getCoordinate(), ithType, userCell);
				}
			}
		}
		
		m_listener.onUserPopulated();
	}

	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getStartingPosition )
		{
			bhPoint startingPosition = new bhPoint();
			startingPosition.readJson(response.getJson());
			
			m_user.getLastPosition().copy(startingPosition);
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.getUserData )
		{
			this.onGetUserDataSuccess(response);
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		
		return bhE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getStartingPosition )
		{
			//--- DRK > Trivial error, so just fail silently.
			m_user.getLastPosition().zeroOut();
			
			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.getUserData )
		{
			clearUser(); // shouldn't be necessary i think...but just in case
			
			if( response.getError() == bhE_ResponseError.NOT_AUTHENTICATED )
			{
				if( m_failedToAuthenticateInBatch )
				{
					//--- DRK > We don't bubble this up to account manager because we "took a chance" by piggy-backing
					//---		the sign in/up request.  If it failed, this request couldn't succeed, but we don't let
					//---		the outside world know about the authentication error.
					//---		This case will be common either when signing up and trying common usernames, or when signing
					//---		in multiple times with a forgotten username/password combo.
					return bhE_ResponseErrorControl.BREAK;
				}
			}
			else
			{
				if( m_authenticatedInBatch || m_accountManager.isSignedIn() )
				{
					m_listener.onGetUserFailed();
					
					return bhE_ResponseErrorControl.BREAK;
				}
			}
		}
		
		return bhE_ResponseErrorControl.CONTINUE;
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
				if( bhClientTransactionManager.getInstance().isInBatch() )
				{
					m_authenticatedInBatch = true;
				}
				
				break;
			}
			
			case SIGN_IN_FAILURE:
			case SIGN_UP_FAILURE:
			case PASSWORD_CONFIRM_FAILURE:
			{
				if( bhClientTransactionManager.getInstance().isInBatch() )
				{
					m_failedToAuthenticateInBatch = true;
				}
				
				break;
			}
		}
	}
	
	private void clearUser()
	{
		bhA_ClientUser user = m_user;
		
		if( user.isPopulated() )
		{
			bhClientTransactionManager.getInstance().cancelRequestsByPath(bhE_RequestPath.getUserData);
			
			user.clearAllLocalChanges();
			
			Iterator<? extends bhUserCell> cellIterator = user.getCells();
			
			bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
			
			while( cellIterator.hasNext() )
			{
				bhUserCell userCell = cellIterator.next();
				bhGridCoordinate coord = userCell.getCoordinate();
				
				if( buffer.getCellSize() == 1 )
				{
					if( buffer.isInBoundsAbsolute(coord) )
					{
						bhBufferCell bufferCell = buffer.getCellAtAbsoluteCoord(coord);
						bhCode sourceCode = userCell.getCode(bhE_CodeType.SOURCE);
						
						if( sourceCode != null )
						{
							bufferCell.setCode(bhE_CodeType.SOURCE, sourceCode);
						}
						
						if( bufferCell.isFocused() )
						{
							bhCode compiledCode = userCell.getCode(bhE_CodeType.COMPILED);
							
							if( compiledCode != null )
							{
								if( bufferCell.hasBeenPreviewed() )
								{
									//--- DRK > This makes sure that previews are cleared if the user is looking at previewed code
									//---		at their own cell and they sign out.
									bufferCell.setCode(bhE_CodeType.COMPILED, compiledCode);
								}
							}
						}
					}
				}
				
				//--- DRK > Just dumping all we can into other local code repositories
				//---		because it won't be available in user object anymore.
				bhCellCodeCache cache = bhCellCodeCache.getInstance();
				cache.cacheCell(userCell);
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
