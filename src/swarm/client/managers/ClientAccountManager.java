package swarm.client.managers;

import java.util.ArrayList;

import swarm.client.app.AppContext;
import swarm.client.managers.CellCodeManager.I_Listener;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.structs.AccountInfo;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignInValidator;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.account.SignUpValidator;
import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_EditingPermission;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.reflection.I_Callback;
import swarm.shared.statemachine.A_State;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;
import com.google.gwt.http.client.RequestBuilder;

public class ClientAccountManager implements I_TransactionResponseHandler
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
	
	private SignUpValidationResult m_latestBadSignUpResult = null;
	private SignInValidationResult m_latestBadSignInResult = null;
	
	private AccountInfo m_accountInfo = null;
	
	private String m_passwordChangeToken = null;
	
	private E_PasswordChangeTokenState m_passwordChangeTokenState = null;
	
	private final ArrayList<I_Delegate> m_delegates = new ArrayList<I_Delegate>();
	
	private I_Callback m_initCallback = null;
	
	private final ClientTransactionManager m_txnMngr;
	private final A_JsonFactory m_jsonFactory;
	private final SignInValidator m_signInValidator;
	private final SignUpValidator m_signUpValidator;
	
	public ClientAccountManager(SignInValidator signInValidator, SignUpValidator signUpValidator, ClientTransactionManager txnMngr, A_JsonFactory jsonFactory)
	{
		m_signInValidator = signInValidator;
		m_signUpValidator = signUpValidator;
		m_txnMngr = txnMngr;
		m_jsonFactory = jsonFactory;
	}
	
	public AccountInfo getAccountInfo()
	{
		return m_accountInfo;
	}
	
	public SignInValidator getSignInValidator()
	{
		return m_signInValidator;
	}
	
	public SignUpValidator getSignUpValidator()
	{
		return m_signUpValidator;
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
		
		U_Debug.ASSERT(false, "smAcountManager::removeDelegate");
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
		m_txnMngr.addHandler(this);
	}
	
	public void stop()
	{
		m_txnMngr.removeHandler(this);
	}
	
	public SignUpValidationResult checkOutLatestBadSignUpResult()
	{
		SignUpValidationResult result = m_latestBadSignUpResult;
		m_latestBadSignUpResult = null;
		
		return result;
	}
	
	public SignInValidationResult checkOutLatestBadSignInResult()
	{
		SignInValidationResult result = m_latestBadSignInResult;
		m_latestBadSignInResult = null;
		
		return result;
	}
	
	public void signUp(SignUpCredentials credentials, E_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			U_Debug.ASSERT(false, "signUp1");
			return;
		}
		
		m_txnMngr.performAction(action, E_RequestPath.signUp, credentials);
	}
	
	public void init(I_Callback callback)
	{
		m_initCallback = callback;
		
		if( isWaitingOnServer() )
		{
			U_Debug.ASSERT(false, "init client AM");
			return;
		}

		m_txnMngr.performAction(E_TransactionAction.QUEUE_REQUEST, E_RequestPath.getAccountInfo);
		m_txnMngr.performAction(E_TransactionAction.QUEUE_REQUEST_AND_FLUSH, E_RequestPath.getPasswordChangeToken);
	}
	
	public void setNewDesiredPassword(SignInCredentials creds)
	{
		m_passwordChangeToken = null;
		
		if( isWaitingOnServer() || m_isSignedIn )
		{
			U_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		m_txnMngr.makeRequest(E_RequestPath.setNewDesiredPassword, creds);
	}
	
	
	public void signIn(SignInCredentials credentials, E_TransactionAction action)
	{
		if( isWaitingOnServer() || m_isSignedIn )
		{
			U_Debug.ASSERT(false, "signIn1");
			return;
		}
		
		if( m_passwordChangeToken != null )
		{
			credentials.setPasswordChangeToken(m_passwordChangeToken);
		}
		
		m_txnMngr.performAction(E_TransactionAction.QUEUE_REQUEST, E_RequestPath.signIn, credentials);
		
		action = action == E_TransactionAction.MAKE_REQUEST ? E_TransactionAction.QUEUE_REQUEST_AND_FLUSH : action;
		m_txnMngr.performAction(action, E_RequestPath.getAccountInfo);
	}
	
	public void signOut()
	{
		if( isWaitingOnServer() || !m_isSignedIn )
		{
			U_Debug.ASSERT(false, "signOut3");
			
			return;
		}
		
		m_txnMngr.makeRequest(E_RequestPath.signOut);
	}
	
	public boolean isWaitingOnServer()
	{
		return getWaitReason() != E_WaitReason.NONE;
	}
	
	public E_WaitReason getWaitReason()
	{
		ClientTransactionManager manager = m_txnMngr;
		
		if( manager.containsDispatchedRequest(E_RequestPath.signIn) )
		{
			return E_WaitReason.SIGNING_IN;
		}
		else if( manager.containsDispatchedRequest(E_RequestPath.signUp) )
		{
			return E_WaitReason.SIGNING_UP;
		}
		else if( manager.containsDispatchedRequest(E_RequestPath.signOut) )
		{
			return E_WaitReason.SIGNING_OUT;
		}
		else if( manager.containsDispatchedRequest(E_RequestPath.setNewDesiredPassword) )
		{
			return E_WaitReason.SETTING_NEW_PASSWORD;
		}
		
		return E_WaitReason.NONE;
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getAccountInfo )
		{
			ClientTransactionManager manager = m_txnMngr;
			
			if( manager.hasPreviousBatchResponse(E_RequestPath.signIn) )
			{				
				TransactionResponse signInResponse = manager.getPreviousBatchResponse(E_RequestPath.signIn);
				
				SignInValidationResult result = new SignInValidationResult();
				
				if( signInResponse.getError() == E_ResponseError.NO_ERROR )
				{
					result.readJson(signInResponse.getJsonArgs(), this.m_jsonFactory);
					
					if( result.isEverythingOk() )
					{
						m_accountInfo = new AccountInfo();
						m_accountInfo.readJson(response.getJsonArgs(), this.m_jsonFactory);
						
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
				m_accountInfo = new AccountInfo();
				m_accountInfo.readJson(response.getJsonArgs(), m_jsonFactory);
			}
			
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.signIn )
		{
			//--- Deferred until getAccountInfo handler.
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.signUp )
		{
			SignUpValidationResult result = new SignUpValidationResult(m_jsonFactory, response.getJsonArgs());
			
			if( result.isEverythingOk() )
			{
				//--- DRK > Populate account info.
				SignUpCredentials	creds = new SignUpCredentials(m_jsonFactory, request.getJsonArgs());
				m_accountInfo = new AccountInfo();
				m_accountInfo.copyCredentials(creds);
				
				onResponse(E_ResponseType.SIGN_UP_SUCCESS);
			}
			else
			{
				m_latestBadSignUpResult = result;
				
				onResponse(E_ResponseType.SIGN_UP_FAILURE);
			}
			
			return E_ResponseSuccessControl.BREAK;
		}
			
		else if( request.getPath() == E_RequestPath.signOut )
		{
			onResponse(E_ResponseType.SIGN_OUT_SUCCESS);
			
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.setNewDesiredPassword )
		{
			onResponse(E_ResponseType.PASSWORD_CHANGE_SUCCESS);
			
			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.getPasswordChangeToken )
		{
			 //--- DRK > Can be (usually is) null.
			m_passwordChangeToken = m_jsonFactory.getHelper().getString(response.getJsonArgs(), E_JsonKey.passwordChangeToken);
			
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
			
			return E_ResponseSuccessControl.BREAK;
		}

		return E_ResponseSuccessControl.CONTINUE;
	}
	
	public E_PasswordChangeTokenState getPasswordChangeTokenState()
	{
		return m_passwordChangeTokenState;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getAccountInfo )
		{
			ClientTransactionManager manager = m_txnMngr;
			
			if( manager.hasPreviousBatchResponse(E_RequestPath.signIn) )
			{
				TransactionResponse previousResponse = manager.getPreviousBatchResponse(E_RequestPath.signIn);
				
				m_latestBadSignInResult = new SignInValidationResult();
				
				if( previousResponse.getError() == E_ResponseError.NO_ERROR )
				{
					m_latestBadSignInResult.readJson(previousResponse.getJsonArgs(), this.m_jsonFactory);
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
			
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return E_ResponseErrorControl.CONTINUE;
			}

			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.signIn )
		{
			//--- Deferred for getAccountInfo handler...we're making a pretty safe assumption here
			//--- that either both responses have a VERSION_MISMATCH error, or neither.
			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.signUp )
		{
			m_latestBadSignUpResult = new SignUpValidationResult();
			m_latestBadSignUpResult.setResponseError();
			
			onResponse(E_ResponseType.SIGN_UP_FAILURE);

			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				//--- DRK > Let base controller take care of this and blow up.
				return E_ResponseErrorControl.CONTINUE;
			}
			
			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.signOut )
		{
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.SIGN_OUT_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return E_ResponseErrorControl.CONTINUE;
			}
			else if( response.getError() == E_ResponseError.NOT_AUTHENTICATED )
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
			
			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.setNewDesiredPassword )
		{
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);
				
				//--- DRK > Let base controller take care of this and blow up.
				return E_ResponseErrorControl.CONTINUE;
			}

			onResponse(E_ResponseType.PASSWORD_CHANGE_FAILURE);

			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.getPasswordChangeToken )
		{
			m_passwordChangeToken = null;
			m_passwordChangeTokenState = E_PasswordChangeTokenState.INVALID;
			
			if( m_initCallback != null )
			{
				m_initCallback.invoke();
			}
			
			return E_ResponseErrorControl.BREAK;
		}
		else
		{
			//--- DRK > This acts as a filter for all requests, so unless some response handler 
			//---		lower on the totum pole catches this, this will cause an app-wide authentication
			//---		error.
			if( response.getError() == E_ResponseError.NOT_AUTHENTICATED )
			{
				onSignOut();
				
				for( int i = 0; i < m_delegates.size(); i++ )
				{
					m_delegates.get(i).onAuthenticationError();
				}
				
				return E_ResponseErrorControl.BREAK;
			}
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}
	
	private void onSignOut()
	{
		m_accountInfo = null;
		m_isSignedIn = false;
	}
}
