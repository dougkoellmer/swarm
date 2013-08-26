package swarm.client.managers;

import java.util.ArrayList;

import swarm.client.app.sm_c;
import swarm.client.managers.smCellCodeManager.I_SyncOrPreviewDelegate;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.structs.smAccountInfo;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidationResult;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smSignUpValidationResult;
import swarm.shared.app.sm;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_EditingPermission;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.reflection.smI_Callback;
import swarm.shared.statemachine.smA_State;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import com.google.gwt.http.client.RequestBuilder;

public class smClientAccountManager implements smI_TransactionResponseHandler
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
	
	private boolean m_isSignedIn = false;
	
	private smSignUpValidationResult m_latestBadSignUpResult = null;
	private smSignInValidationResult m_latestBadSignInResult = null;
	
	private smAccountInfo m_accountInfo = null;
	
	private String m_passwordChangeToken = null;
	
	private E_PasswordChangeTokenState m_passwordChangeTokenState = null;
	
	private final ArrayList<I_Delegate> m_delegates = new ArrayList<I_Delegate>();
	
	private smI_Callback m_initCallback = null;
	
	public smClientAccountManager()
	{
		
	}
	
	public smAccountInfo getAccountInfo()
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
		
		smU_Debug.ASSERT(false, "smAcountManager::removeDelegate");
	}
	
	private void onResponse(E_ResponseType type)
	{
		//--- DRK > Now an invalid assert, because transaction manager is more correct
		//---		in telling us that, inside a response handler, a given request
		//---		isn't actually "dispatched" anymore, because it's returned and being handle.
		//smU_Debug.ASSERT(getWaitReason() != E_WaitReason.NONE, "smClientAccountManager::onResponse " + type);
		
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
	
	public void start()
	{
		sm_c.txnMngr.addHandler(this);
	}
	
	public void stop()
	{
		sm_c.txnMngr.removeHandler(this);
	}
	
	public smSignUpValidationResult checkOutLatestBadSignUpResult()
	{
		smSignUpValidationResult result = m_latestBadSignUpResult;
		m_latestBadSignUpResult = null;
		
		return result;
	}
	
	public smSignInValidationResult checkOutLatestBadSignInResult()
	{
		smSignInValidationResult result = m_latestBadSignInResult;
		m_latestBadSignInResult = null;
		
		return result;
	}
	
	public void signUp(smSignUpCredentials credentials, smE_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			smU_Debug.ASSERT(false, "signUp1");
			return;
		}
		
		smTransactionRequest request = new smTransactionRequest(smE_RequestPath.signUp);
		credentials.writeJson(request.getJson());
		
		sm_c.txnMngr.performAction(action, request);
	}
	
	public void init(smI_Callback callback)
	{
		m_initCallback = callback;
		
		if( isWaitingOnServer() )
		{
			smU_Debug.ASSERT(false, "init client AM");
			return;
		}

		sm_c.txnMngr.performAction(smE_TransactionAction.QUEUE_REQUEST, smE_RequestPath.getAccountInfo);
		sm_c.txnMngr.performAction(smE_TransactionAction.QUEUE_REQUEST_AND_FLUSH, smE_RequestPath.getPasswordChangeToken);
	}
	
	public void setNewDesiredPassword(smSignInCredentials creds)
	{
		m_passwordChangeToken = null;
		
		if( isWaitingOnServer() || m_isSignedIn )
		{
			smU_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		sm_c.txnMngr.makeRequest(smE_RequestPath.setNewDesiredPassword, creds);
	}
	
	
	public void signIn(smSignInCredentials credentials, smE_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			smU_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		smTransactionRequest request = new smTransactionRequest(smE_RequestPath.signIn);
		credentials.writeJson(request.getJson());
		
		if( m_passwordChangeToken != null )
		{
			sm.jsonFactory.getHelper().putString(request.getJson(), smE_JsonKey.passwordChangeToken, m_passwordChangeToken);
		}
		
		sm_c.txnMngr.performAction(smE_TransactionAction.QUEUE_REQUEST, request);
		
		action = action == smE_TransactionAction.MAKE_REQUEST ? smE_TransactionAction.QUEUE_REQUEST_AND_FLUSH : action;
		sm_c.txnMngr.performAction(action, smE_RequestPath.getAccountInfo);
	}
	
	public void signOut()
	{
		if( isWaitingOnServer() || !m_isSignedIn )
		{
			smU_Debug.ASSERT(false, "signOut3");
			
			return;
		}
		
		smTransactionRequest request = new smTransactionRequest(smE_RequestPath.signOut);

		sm_c.txnMngr.makeRequest(request);
	}
	
	public boolean isWaitingOnServer()
	{
		return getWaitReason() != E_WaitReason.NONE;
	}
	
	public E_WaitReason getWaitReason()
	{
		smClientTransactionManager manager = sm_c.txnMngr;
		
		if( manager.containsDispatchedRequest(smE_RequestPath.signIn) )
		{
			return E_WaitReason.SIGNING_IN;
		}
		else if( manager.containsDispatchedRequest(smE_RequestPath.signUp) )
		{
			return E_WaitReason.SIGNING_UP;
		}
		else if( manager.containsDispatchedRequest(smE_RequestPath.signOut) )
		{
			return E_WaitReason.SIGNING_OUT;
		}
		else if( manager.containsDispatchedRequest(smE_RequestPath.setNewDesiredPassword) )
		{
			return E_WaitReason.SETTING_NEW_PASSWORD;
		}
		
		return E_WaitReason.NONE;
	}

	@Override
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getAccountInfo )
		{
			smClientTransactionManager manager = sm_c.txnMngr;
			
			if( manager.hasPreviousBatchResponse(smE_RequestPath.signIn) )
			{				
				smTransactionResponse signInResponse = manager.getPreviousBatchResponse(smE_RequestPath.signIn);
				
				smSignInValidationResult result = new smSignInValidationResult();
				
				if( signInResponse.getError() == smE_ResponseError.NO_ERROR )
				{
					result.readJson(signInResponse.getJson());
					
					if( result.isEverythingOk() )
					{
						m_accountInfo = new smAccountInfo();
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
				m_accountInfo = new smAccountInfo();
				m_accountInfo.readJson(response.getJson());
			}
			
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.signIn )
		{
			//--- Deferred until getAccountInfo handler.
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.signUp )
		{
			smSignUpValidationResult result = new smSignUpValidationResult(response.getJson());
			
			if( result.isEverythingOk() )
			{
				//--- DRK > Populate account info.
				smSignUpCredentials	creds = new smSignUpCredentials(request.getJson());
				m_accountInfo = new smAccountInfo();
				m_accountInfo.copyCredentials(creds);
				
				onResponse(E_ResponseType.SIGN_UP_SUCCESS);
			}
			else
			{
				m_latestBadSignUpResult = result;
				
				onResponse(E_ResponseType.SIGN_UP_FAILURE);
			}
			
			return smE_ResponseSuccessControl.BREAK;
		}
			
		else if( request.getPath() == smE_RequestPath.signOut )
		{
			onResponse(E_ResponseType.SIGN_OUT_SUCCESS);
			
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.setNewDesiredPassword )
		{
			onResponse(E_ResponseType.PASSWORD_CHANGE_SUCCESS);
			
			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.getPasswordChangeToken )
		{
			 //--- DRK > Can be (usually is) null.
			m_passwordChangeToken = sm.jsonFactory.getHelper().getString(response.getJson(), smE_JsonKey.passwordChangeToken);
			
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
			
			return smE_ResponseSuccessControl.BREAK;
		}

		return smE_ResponseSuccessControl.CONTINUE;
	}
	
	public E_PasswordChangeTokenState getPasswordChangeTokenState()
	{
		return m_passwordChangeTokenState;
	}

	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getAccountInfo )
		{
			smClientTransactionManager manager = sm_c.txnMngr;
			
			if( manager.hasPreviousBatchResponse(smE_RequestPath.signIn) )
			{
				smTransactionResponse previousResponse = manager.getPreviousBatchResponse(smE_RequestPath.signIn);
				
				m_latestBadSignInResult = new smSignInValidationResult();
				
				if( previousResponse.getError() == smE_ResponseError.NO_ERROR )
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
			
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return smE_ResponseErrorControl.CONTINUE;
			}

			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.signIn )
		{
			//--- Deferred for getAccountInfo handler...we're making a pretty safe assumption here
			//--- that either both responses have a VERSION_MISMATCH error, or neither.
			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.signUp )
		{
			m_latestBadSignUpResult = new smSignUpValidationResult();
			m_latestBadSignUpResult.setResponseError();
			
			onResponse(E_ResponseType.SIGN_UP_FAILURE);

			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return smE_ResponseErrorControl.CONTINUE;
			}
			
			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.signOut )
		{
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.SIGN_OUT_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return smE_ResponseErrorControl.CONTINUE;
			}
			else if( response.getError() == smE_ResponseError.NOT_AUTHENTICATED )
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
			
			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.setNewDesiredPassword )
		{
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return smE_ResponseErrorControl.CONTINUE;
			}

			onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);

			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.getPasswordChangeToken )
		{
			m_passwordChangeToken = null;
			m_passwordChangeTokenState = E_PasswordChangeTokenState.INVALID;
			
			if( m_initCallback != null )
			{
				m_initCallback.invoke();
			}
			
			return smE_ResponseErrorControl.BREAK;
		}
		else
		{
			//--- DRK > This acts as a filter for all requests, so unless some response handler 
			//---		lower on the totum pole catches this, this will cause an app-wide authentication
			//---		error.
			if( response.getError() == smE_ResponseError.NOT_AUTHENTICATED )
			{
				onSignOut();
				
				for( int i = 0; i < m_delegates.size(); i++ )
				{
					m_delegates.get(i).onAuthenticationError();
				}
				
				return smE_ResponseErrorControl.BREAK;
			}
		}
		
		return smE_ResponseErrorControl.CONTINUE;
	}
	
	private void onSignOut()
	{
		m_accountInfo = null;
		m_isSignedIn = false;
	}
}
