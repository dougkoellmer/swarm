package swarm.server.session;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import swarm.server.account.E_Role;
import swarm.server.account.S_ServerAccount;
import swarm.server.account.UserSession;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.data.blob.U_Serialization;
import swarm.server.transaction.I_TransactionScopeListener;
import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class SessionManager implements I_TransactionScopeListener
{
	private static final Logger s_logger = Logger.getLogger(SessionManager.class.getName());
		
	private final URLCodec m_urlCodec = new URLCodec(); // thread safe
	
	private I_BlobManager m_blobManager;
	
	private final ThreadLocal<UserSession> m_sessionCache = new ThreadLocal<UserSession>();
	
	private final UserSession m_nullSession = new UserSession();
	private final A_JsonFactory m_jsonFactory;
	
	public SessionManager(BlobManagerFactory blobMngrFactory, A_JsonFactory jsonFactory)
	{
		m_jsonFactory = jsonFactory;
		m_blobManager = blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE, E_BlobCacheLevel.PERSISTENT);
	}
	
	private SessionCookieValue createSessionCookieValue(TransactionResponse response, UserSession userSession, E_SessionType type)
	{
		HttpServletResponse nativeResponse = ((HttpServletResponse) response.getNativeResponse());
		SessionCookieValue cookieValue = new SessionCookieValue(userSession.getAccountId(), type);
		I_JsonObject jsonObject = m_jsonFactory.createJsonObject();
		cookieValue.writeJson(jsonObject, m_jsonFactory);
		String cookieValueJson = jsonObject.writeString();
		
		try
		{
			cookieValueJson = m_urlCodec.encode(cookieValueJson);
		}
		catch (EncoderException e)
		{
			s_logger.log(Level.SEVERE, "Could not start persistent session because could not encode cookie value: " + cookieValueJson, e);
			
			return null;
		}
		
		if( type == E_SessionType.PERSISTENT )
		{
			U_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, S_Session.SESSION_TIMEOUT, true);
		}
		else
		{
			U_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, true);
		}
		
		return cookieValue;
	}
	
	private SessionCookieValue getSessionCookieValue(TransactionRequest request, E_SessionType type)
	{
		HttpServletRequest nativeRequest = ((HttpServletRequest) request.getNativeRequest());
		Cookie sessionCookie = U_Cookie.get(nativeRequest, type.getCookieName());
		SessionCookieValue cookieValue = null;
		
		if( sessionCookie != null )
		{
			if( U_Cookie.isDeleted(sessionCookie) )
			{
				//--- A cookie that is "deleted" gets its expiration time set to the past and the browser
				//--- should remove it from disk immediately, or at least never send it back up.
				s_logger.log(Level.SEVERE, "Deleted cookie was sent back to server.");
				
				return null;
			}
			
			String cookieValueJson = null;
			
			try
			{
				cookieValueJson = m_urlCodec.decode(sessionCookie.getValue());
			}
			catch (DecoderException e)
			{
				s_logger.log(Level.SEVERE, "Could not url-decode cookie data  ("+cookieValueJson+").", e);
				
				return null;
			}
			
			//--- DRK > Just being safe here in case someone is spamming invalid cookie data.
			try
			{
				A_JsonFactory jsonFactory = m_jsonFactory;
				I_JsonObject json = jsonFactory.createJsonObject(cookieValueJson);
				cookieValue = new SessionCookieValue(jsonFactory, json, type);
			}
			catch(Exception e)
			{
				s_logger.log(Level.SEVERE, "Could not parse cookie value's json ("+cookieValueJson+").", e);
			}
		}
		
		return cookieValue;
	}
	
	public void startSession(UserSession userSession, Object nativeResponse, boolean rememberMe)
	{
		this.startSession(userSession, new TransactionResponse(m_jsonFactory, nativeResponse), rememberMe);
	}
	
	public void startSession(UserSession userSession, TransactionResponse response, boolean rememberMe)
	{
		SessionCookieValue transCookieValue = createSessionCookieValue(response, userSession, E_SessionType.TRANSIENT);
		SessionCookieValue persCookieValue = rememberMe ? createSessionCookieValue(response, userSession, E_SessionType.PERSISTENT) : null;
		
		setCachedSession(userSession);
		
		if( transCookieValue != null )
		{
			try
			{
				if( persCookieValue == null )
				{
					m_blobManager.putBlobAsync(transCookieValue, userSession);
				}
				else
				{
					HashMap<I_BlobKey, I_Blob> map = new HashMap<I_BlobKey, I_Blob>();
					map.put(transCookieValue, userSession);
					map.put(persCookieValue, userSession);
					
					m_blobManager.putBlobsAsync(map);
				}
			}
			catch(BlobException e)
			{
				s_logger.log(Level.SEVERE, "Could not put session(s) into database for " + userSession.getAccountIdString());
			}
		}
	}
	
	private UserSession getSessionByCookieValue(SessionCookieValue cookieValue)
	{
		UserSession userSession = null;
		
		try
		{
			userSession = m_blobManager.getBlob(cookieValue, UserSession.class);
		}
		catch(BlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not get session by type.", e);
		}
		
		if( userSession != null )
		{
			//--- DRK > See if account id associated with the cookie value matches what's in DB.
			//---		If there isn't a match, it can mean two things that I can think of.
			//---			(1) a hack or DoS attack of some kind, where they're randomly guessing session
			//---				tokens and got one right, but didn't get the account id right.
			//---			(2) An identical session token was generated for two users, meaning a random
			//---				number out of 128-bits was rolled twice, in which case a session was stomped.
			//---		The second is obviously highly unlikely...well, so should be the first.
			if( !cookieValue.getAccountId().equals(userSession.getAccountId()) )
			{
				userSession = null;
				
				s_logger.log(Level.SEVERE, "A user session's account id didn't match the cookie value's. " + cookieValue.getAccountId() + " " + userSession.getAccountId());
			}
			else
			{
				if( userSession.isExpired(S_Session.SESSION_TIMEOUT) )
				{
					userSession = null;
					
					try
					{
						m_blobManager.deleteBlobAsync(cookieValue, UserSession.class);
					}
					catch(BlobException e)
					{
						s_logger.log(Level.SEVERE, "Could not delete expired session from db.", e);
					}
				}
			}
		}
		
		return userSession;
	}
	
	private UserSession getSessionByType(TransactionRequest request, E_SessionType type)
	{
		SessionCookieValue cookieValue = this.getSessionCookieValue(request, type);
		
		if( cookieValue == null )
		{
			return null;
		}
		
		return this.getSessionByCookieValue(cookieValue);
	}
	
	public UserSession getSession(TransactionRequest request, TransactionResponse response)
	{
		//--- DRK > Early-out for when we have a session cached here.
		UserSession userSession = m_sessionCache.get();
		if( userSession != null )
		{
			if( userSession == m_nullSession )
			{
				return null;
			}
			else
			{
				return userSession;
			}
		}
		
		userSession = this.getSessionByType(request, E_SessionType.TRANSIENT);
		
		if( userSession == null)
		{
			SessionCookieValue cookieValue = this.getSessionCookieValue(request, E_SessionType.PERSISTENT);
			
			if( cookieValue != null )
			{
				userSession = this.getSessionByCookieValue(cookieValue);
				
				if( userSession != null )
				{
					//--- DRK > Recreating the persistent session for security purposes (instead of reusing old one)...it got cleared above.
					//----		We assume the user wants to be remembered, because they were remembered before.
					this.startSession(userSession, response, /*rememberMe*/true);
					
					try
					{
						m_blobManager.deleteBlobAsync(cookieValue, UserSession.class);
					}
					catch(BlobException e)
					{
						s_logger.log(Level.WARNING, "Could not delete persistent user session from database.", e);
					}
				}
			}
		}
		
		if( userSession != null )
		{
			setCachedSession(userSession);
		}
		else
		{
			setCachedSession(m_nullSession);
		}
	
		return userSession;
	}
	
	public void endSession(Object nativeRequest, Object nativeResponse)
	{
		this.endSession(new TransactionRequest(m_jsonFactory, nativeRequest), new TransactionResponse(m_jsonFactory, nativeResponse));
	}
	
	public void endSession(TransactionRequest request, TransactionResponse response)
	{
		SessionCookieValue transCookieValue = getSessionCookieValue(request, E_SessionType.TRANSIENT);
		SessionCookieValue persCookieValue = getSessionCookieValue(request, E_SessionType.PERSISTENT);

		setCachedSession(m_nullSession);
		
		try
		{
			if( transCookieValue != null )
			{
				U_Cookie.delete((HttpServletResponse) response.getNativeResponse(), E_SessionType.TRANSIENT.getCookieName());
				
				if( persCookieValue == null )
				{
					m_blobManager.deleteBlobAsync(transCookieValue, UserSession.class);
				}
				else
				{
					U_Cookie.delete((HttpServletResponse) response.getNativeResponse(), E_SessionType.PERSISTENT.getCookieName());
					
					HashMap<I_BlobKey, Class<? extends I_Blob>> map = new HashMap<I_BlobKey, Class<? extends I_Blob>>();
					map.put(transCookieValue, UserSession.class);
					map.put(persCookieValue, UserSession.class);
					m_blobManager.deleteBlobsAsync(map);
				}
			}
			else
			{
				s_logger.warning("Transient cookie value null when attempting to end session.");
				
				if( persCookieValue != null )
				{
					U_Cookie.delete((HttpServletResponse) response.getNativeResponse(), E_SessionType.PERSISTENT.getCookieName());
					
					m_blobManager.deleteBlobAsync(persCookieValue, UserSession.class);
				}
			}
		}
		catch(BlobException e)
		{
			s_logger.log(Level.SEVERE, "Exception occured while trying to delete session(s) from database.");
		}
	}
	
	public boolean isSessionActive(TransactionRequest request, TransactionResponse response)
	{
		UserSession session = this.getSession(request, response);
		
		return session != null;
	}
	
	public boolean isAuthorized(Object nativeRequest, Object nativeResponse, E_Role requiredRole)
	{
		//--- DRK > Just creating dummy request/response wrappers here because caller obviously doesn't care about response.
		return isAuthorized(new TransactionRequest(m_jsonFactory, nativeRequest), new TransactionResponse(m_jsonFactory, nativeResponse), requiredRole);
	}
	
	public boolean isAuthenticated(Object nativeRequest, Object nativeResponse)
	{
		return isAuthorized(nativeRequest, nativeResponse, E_Role.USER);
	}
	
	public boolean isAuthorized(TransactionRequest request, TransactionResponse response, E_Role requiredRole)
	{
		boolean authorized = false;
		boolean authenticated = false;
		
		UserSession userSession = getSession(request, response);
		
		if( userSession != null )
		{
			authenticated = true;
			
			if( userSession.getRole().ordinal() >= requiredRole.ordinal() )
			{
				authorized = true;
			}
		}

		if( !authenticated )
		{
			response.setError(E_ResponseError.NOT_AUTHENTICATED);
		}
		else if( !authorized )
		{
			response.setError(E_ResponseError.NOT_AUTHORIZED);
		}
		
		return authorized;
	}
	
	private void setCachedSession(UserSession session)
	{
		m_sessionCache.set(session);
	}
	
	private void deleteThreadLocalSession()
	{
		m_sessionCache.remove();
	}

	@Override
	public void onEnterScope()
	{
		deleteThreadLocalSession();
	}

	@Override
	public void onBatchStart()
	{
	}

	@Override
	public void onBatchEnd()
	{
		deleteThreadLocalSession();
	}

	@Override
	public void onExitScope()
	{
		deleteThreadLocalSession();
	}
}
