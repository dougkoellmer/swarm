package com.b33hive.client.managers;

import java.util.ArrayList;

import com.b33hive.client.managers.bhCellCodeManager.I_SyncOrPreviewDelegate;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.states.State_AsyncDialog;
import com.b33hive.client.structs.bhAccountInfo;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.client.transaction.bhE_ResponseErrorControl;
import com.b33hive.client.transaction.bhE_ResponseSuccessControl;
import com.b33hive.client.transaction.bhI_TransactionResponseHandler;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.shared.account.bhSignInCredentials;
import com.b33hive.shared.account.bhSignInValidationResult;
import com.b33hive.shared.account.bhSignUpCredentials;
import com.b33hive.shared.account.bhSignUpValidationResult;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.entities.bhE_EditingPermission;
import com.b33hive.shared.json.bhA_JsonEncodable;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.reflection.bhI_Callback;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
import com.google.gwt.http.client.RequestBuilder;

public class bhClientAccountManager implements bhI_TransactionResponseHandler
{
	public static enum E_PasswordChangeTokenState
	{
		NONE, INVALID, VALID;
	}
	
	public static enum E_WaitReason
	{
		NONE,
		SIGNING_IN,
		SIGNING_UP,
		SIGNING_OUT,
		SETTING_NEW_PASSWORD,
		CONFIRMING_NEW_PASSWORD;
	}
	
	public static enum E_ResponseType
	{
		SIGN_IN_SUCCESS,
		SIGN_IN_FAILURE,
		SIGN_UP_SUCCESS,
		SIGN_UP_FAILURE,
		SIGN_OUT_SUCCESS,
		SIGN_OUT_FAILURE,
		PASSWORD_CHANGE_SUCCESS,
		PASSWORD_CHANGE_FAILURE,
		PASSWORD_CONFIRM_SUCCESS,
		PASSWORD_CONFIRM_FAILURE;
		
		public boolean isGood()
		{
			return	this == SIGN_OUT_SUCCESS || this == SIGN_IN_SUCCESS || this == SIGN_UP_SUCCESS ||
					this == PASSWORD_CONFIRM_SUCCESS || this == PASSWORD_CHANGE_SUCCESS;
		}
	}
	
	public static interface I_Delegate
	{
		void onAccountTransactionResponse(E_ResponseType type);
		
		void onAuthenticationError();
	}
	
	private static bhClientAccountManager s_instance = null;
	
	private boolean m_isSignedIn = false;
	
	private bhSignUpValidationResult m_latestBadSignUpResult = null;
	private bhSignInValidationResult m_latestBadSignInResult = null;
	
	private bhAccountInfo m_accountInfo = null;
	
	private String m_passwordChangeToken = null;
	
	private E_PasswordChangeTokenState m_passwordChangeTokenState = null;
	
	private final ArrayList<I_Delegate> m_delegates = new ArrayList<I_Delegate>();
	
	private bhI_Callback m_initCallback = null;
	
	private bhClientAccountManager()
	{
		
	}
	
	public bhAccountInfo getAccountInfo()
	{
		return m_accountInfo;
	}
	
	public boolean isSignedIn()
	{
		return m_isSignedIn;
	}
	
	public void addDelegate(I_Delegate delegate)
	{
		m_delegates.add(delegate);
	}
	
	public void removeDelegate(I_Delegate delegate)
	{
		for( int i = m_delegates.size()-1; i >= 0; i-- )
		{
			if( m_delegates.get(i) == delegate )
			{
				m_delegates.remove(i);
				
				return;
			}
		}
		
		bhU_Debug.ASSERT(false, "bhAcountManager::removeDelegate");
	}
	
	private void onResponse(E_ResponseType type)
	{
		//--- DRK > Now an invalid assert, because transaction manager is more correct
		//---		in telling us that, inside a response handler, a given request
		//---		isn't actually "dispatched" anymore, because it's returned and being handle.
		//bhU_Debug.ASSERT(getWaitReason() != E_WaitReason.NONE, "bhClientAccountManager::onResponse " + type);
		
		switch( type)
		{
			case SIGN_OUT_SUCCESS:
			{
				onSignOut();
				
				break;
			}
			
			case SIGN_IN_SUCCESS:
			case SIGN_UP_SUCCESS:
			case PASSWORD_CONFIRM_SUCCESS:
			{
				m_isSignedIn = true;
				
				m_latestBadSignInResult = null;
				m_latestBadSignUpResult = null;
				
				break;
			}
		}
		
		if( m_passwordChangeToken != null )
		{
			if( type == E_ResponseType.SIGN_IN_SUCCESS )
			{
				type = E_ResponseType.PASSWORD_CONFIRM_SUCCESS;
			}
			else if( type == E_ResponseType.SIGN_IN_FAILURE )
			{
				m_latestBadSignInResult = null;
				
				type = E_ResponseType.PASSWORD_CONFIRM_FAILURE;
			}
			
			m_passwordChangeToken = null;
		}
		
		for( int i = m_delegates.size()-1; i >= 0; i-- )
		{
			m_delegates.get(i).onAccountTransactionResponse(type);
		}
	}
	
	public static void startUp()
	{
		s_instance = new bhClientAccountManager();
	}
	
	public void start()
	{
		bhClientTransactionManager.getInstance().addHandler(this);
	}
	
	public void stop()
	{
		bhClientTransactionManager.getInstance().removeHandler(this);
	}
	
	public static bhClientAccountManager getInstance()
	{
		return s_instance;
	}
	
	public bhSignUpValidationResult checkOutLatestBadSignUpResult()
	{
		bhSignUpValidationResult result = m_latestBadSignUpResult;
		m_latestBadSignUpResult = null;
		
		return result;
	}
	
	public bhSignInValidationResult checkOutLatestBadSignInResult()
	{
		bhSignInValidationResult result = m_latestBadSignInResult;
		m_latestBadSignInResult = null;
		
		return result;
	}
	
	public void signUp(bhSignUpCredentials credentials, bhE_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			bhU_Debug.ASSERT(false, "signUp1");
			return;
		}
		
		bhTransactionRequest request = new bhTransactionRequest(bhE_RequestPath.signUp);
		credentials.writeJson(request.getJson());
		
		bhClientTransactionManager.getInstance().performAction(action, request);
	}
	
	public void init(bhI_Callback callback)
	{
		m_initCallback = callback;
		
		if( isWaitingOnServer() )
		{
			bhU_Debug.ASSERT(false, "init client AM");
			return;
		}

		bhClientTransactionManager.getInstance().performAction(bhE_TransactionAction.QUEUE_REQUEST, bhE_RequestPath.getAccountInfo);
		bhClientTransactionManager.getInstance().performAction(bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH, bhE_RequestPath.getPasswordChangeToken);
	}
	
	public void setNewDesiredPassword(bhSignInCredentials creds)
	{
		m_passwordChangeToken = null;
		
		if( isWaitingOnServer() || m_isSignedIn )
		{
			bhU_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		bhClientTransactionManager.getInstance().makeRequest(bhE_RequestPath.setNewDesiredPassword, creds);
	}
	
	
	public void signIn(bhSignInCredentials credentials, bhE_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			bhU_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		bhTransactionRequest request = new bhTransactionRequest(bhE_RequestPath.signIn);
		credentials.writeJson(request.getJson());
		
		if( m_passwordChangeToken != null )
		{
			bhJsonHelper.getInstance().putString(request.getJson(), bhE_JsonKey.passwordChangeToken, m_passwordChangeToken);
		}
		
		bhClientTransactionManager.getInstance().performAction(bhE_TransactionAction.QUEUE_REQUEST, request);
		
		action = action == bhE_TransactionAction.MAKE_REQUEST ? bhE_TransactionAction.QUEUE_REQUEST_AND_FLUSH : action;
		bhClientTransactionManager.getInstance().performAction(action, bhE_RequestPath.getAccountInfo);
	}
	
	public void signOut()
	{
		if( isWaitingOnServer() || !m_isSignedIn )
		{
			bhU_Debug.ASSERT(false, "signOut3");
			
			return;
		}
		
		bhTransactionRequest request = new bhTransactionRequest(bhE_RequestPath.signOut);

		bhClientTransactionManager.getInstance().makeRequest(request);
	}
	
	public boolean isWaitingOnServer()
	{
		return getWaitReason() != E_WaitReason.NONE;
	}
	
	public E_WaitReason getWaitReason()
	{
		bhClientTransactionManager manager = bhClientTransactionManager.getInstance();
		
		if( manager.containsDispatchedRequest(bhE_RequestPath.signIn) )
		{
			return E_WaitReason.SIGNING_IN;
		}
		else if( manager.containsDispatchedRequest(bhE_RequestPath.signUp) )
		{
			return E_WaitReason.SIGNING_UP;
		}
		else if( manager.containsDispatchedRequest(bhE_RequestPath.signOut) )
		{
			return E_WaitReason.SIGNING_OUT;
		}
		else if( manager.containsDispatchedRequest(bhE_RequestPath.setNewDesiredPassword) )
		{
			return E_WaitReason.SETTING_NEW_PASSWORD;
		}
		
		return E_WaitReason.NONE;
	}

	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getAccountInfo )
		{
			bhClientTransactionManager manager = bhClientTransactionManager.getInstance();
			
			if( manager.hasPreviousBatchResponse(bhE_RequestPath.signIn) )
			{				
				bhTransactionResponse signInResponse = manager.getPreviousBatchResponse(bhE_RequestPath.signIn);
				
				bhSignInValidationResult result = new bhSignInValidationResult();
				
				if( signInResponse.getError() == bhE_ResponseError.NO_ERROR )
				{
					result.readJson(signInResponse.getJson());
					
					if( result.isEverythingOk() )
					{
						m_accountInfo = new bhAccountInfo();
						m_accountInfo.readJson(response.getJson());
						
						onResponse(E_ResponseType.SIGN_IN_SUCCESS);
					}
				}
				else
				{
					result.setResponseError();
				}

				if( !result.isEverythingOk() )
				{
					m_latestBadSignInResult = result;
					
					onResponse(E_ResponseType.SIGN_IN_FAILURE);
				}
			}
			else
			{
				m_isSignedIn = true;
				m_accountInfo = new bhAccountInfo();
				m_accountInfo.readJson(response.getJson());
			}
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.signIn )
		{
			//--- Deferred until getAccountInfo handler.
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.signUp )
		{
			bhSignUpValidationResult result = new bhSignUpValidationResult(response.getJson());
			
			if( result.isEverythingOk() )
			{
				//--- DRK > Populate account info.
				bhSignUpCredentials	creds = new bhSignUpCredentials(request.getJson());
				m_accountInfo = new bhAccountInfo();
				m_accountInfo.copyCredentials(creds);
				
				onResponse(E_ResponseType.SIGN_UP_SUCCESS);
			}
			else
			{
				m_latestBadSignUpResult = result;
				
				onResponse(E_ResponseType.SIGN_UP_FAILURE);
			}
			
			return bhE_ResponseSuccessControl.BREAK;
		}
			
		else if( request.getPath() == bhE_RequestPath.signOut )
		{
			onResponse(E_ResponseType.SIGN_OUT_SUCCESS);
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.setNewDesiredPassword )
		{
			onResponse(E_ResponseType.PASSWORD_CHANGE_SUCCESS);
			
			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.getPasswordChangeToken )
		{
			 //--- DRK > Can be (usually is) null.
			m_passwordChangeToken = bhJsonHelper.getInstance().getString(response.getJson(), bhE_JsonKey.passwordChangeToken);
			
			if( m_passwordChangeToken == null )
			{
				m_passwordChangeTokenState = E_PasswordChangeTokenState.NONE;
			}
			else
			{
				m_passwordChangeTokenState = E_PasswordChangeTokenState.VALID;
			}
			
			if( m_initCallback != null )
			{
				m_initCallback.invoke();
			}
			
			return bhE_ResponseSuccessControl.BREAK;
		}

		return bhE_ResponseSuccessControl.CONTINUE;
	}
	
	public E_PasswordChangeTokenState getPasswordChangeTokenState()
	{
		return m_passwordChangeTokenState;
	}

	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getAccountInfo )
		{
			bhClientTransactionManager manager = bhClientTransactionManager.getInstance();
			
			if( manager.hasPreviousBatchResponse(bhE_RequestPath.signIn) )
			{
				bhTransactionResponse previousResponse = manager.getPreviousBatchResponse(bhE_RequestPath.signIn);
				
				m_latestBadSignInResult = new bhSignInValidationResult();
				
				if( previousResponse.getError() == bhE_ResponseError.NO_ERROR )
				{
					m_latestBadSignInResult.readJson(previousResponse.getJson());
				}
				else
				{
					m_latestBadSignInResult.setResponseError();
				}
				
				onResponse(E_ResponseType.SIGN_IN_FAILURE);
			}
			else
			{
				// DO NOTHING
			}
			
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return bhE_ResponseErrorControl.CONTINUE;
			}

			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.signIn )
		{
			//--- Deferred for getAccountInfo handler...we're making a pretty safe assumption here
			//--- that either both responses have a VERSION_MISMATCH error, or neither.
			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.signUp )
		{
			m_latestBadSignUpResult = new bhSignUpValidationResult();
			m_latestBadSignUpResult.setResponseError();
			
			onResponse(E_ResponseType.SIGN_UP_FAILURE);

			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return bhE_ResponseErrorControl.CONTINUE;
			}
			
			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.signOut )
		{
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.SIGN_OUT_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return bhE_ResponseErrorControl.CONTINUE;
			}
			else if( response.getError() == bhE_ResponseError.NOT_AUTHENTICATED )
			{
				//--- DRK > This is actually a valid sign out success case, because it probably simply meant 
				//---		that the session expired on the server. It is probably somewhat rare, because 
				//---		it requires a user leaving the app open for more than 24 hours (or whatever it is),
				//---		AND immediately coming back and pressing sign out for whatever reason.
				onResponse(E_ResponseType.SIGN_OUT_SUCCESS);
			}
			else
			{
				onResponse(E_ResponseType.SIGN_OUT_FAILURE);
			}
			
			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.setNewDesiredPassword )
		{
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return bhE_ResponseErrorControl.CONTINUE;
			}

			onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);

			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.getPasswordChangeToken )
		{
			m_passwordChangeToken = null;
			m_passwordChangeTokenState = E_PasswordChangeTokenState.INVALID;
			
			if( m_initCallback != null )
			{
				m_initCallback.invoke();
			}
			
			return bhE_ResponseErrorControl.BREAK;
		}
		else
		{
			//--- DRK > This acts as a filter for all requests, so unless some response handler 
			//---		lower on the totum pole catches this, this will cause an app-wide authentication
			//---		error.
			if( response.getError() == bhE_ResponseError.NOT_AUTHENTICATED )
			{
				onSignOut();
				
				for( int i = 0; i < m_delegates.size(); i++ )
				{
					m_delegates.get(i).onAuthenticationError();
				}
				
				return bhE_ResponseErrorControl.BREAK;
			}
		}
		
		return bhE_ResponseErrorControl.CONTINUE;
	}
	
	private void onSignOut()
	{
		m_accountInfo = null;
		m_isSignedIn = false;
	}
}
