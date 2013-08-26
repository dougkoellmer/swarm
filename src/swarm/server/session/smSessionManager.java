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

import swarm.server.account.smE_Role;
import swarm.server.account.smS_ServerAccount;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.data.blob.smU_Serialization;
import swarm.server.transaction.smI_TransactionScopeListener;
import swarm.shared.app.sm;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smSessionManager implements smI_TransactionScopeListener
{
	private static final Logger s_logger = Logger.getLogger(smSessionManager.class.getName());
		
	private final URLCodec m_urlCodec = new URLCodec(); // thread safe
	
	private smI_BlobManager m_blobManager;
	
	private final ThreadLocal<smUserSession> m_sessionCache = new ThreadLocal<smUserSession>();
	
	private final smUserSession m_nullSession = new smUserSession();
	
	public smSessionManager()
	{
		m_blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
	}
	
	private smSessionCookieValue createSessionCookieValue(smTransactionResponse response, smUserSession userSession, smE_SessionType type)
	{
		HttpServletResponse nativeResponse = ((HttpServletResponse) response.getNativeResponse());
		smSessionCookieValue cookieValue = new smSessionCookieValue(userSession.getAccountId(), type);
		String cookieValueJson = cookieValue.writeJson().writeString();
		
		try
		{
			cookieValueJson = m_urlCodec.encode(cookieValueJson);
		}
		catch (EncoderException e)
		{
			s_logger.log(Level.SEVERE, "Could not start persistent session because could not encode cookie value: " + cookieValueJson, e);
			
			return null;
		}
		
		if( type == smE_SessionType.PERSISTENT )
		{
			smU_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, smS_Session.SESSION_TIMEOUT, true);
		}
		else
		{
			smU_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, true);
		}
		
		return cookieValue;
	}
	
	private smSessionCookieValue getSessionCookieValue(smTransactionRequest request, smE_SessionType type)
	{
		HttpServletRequest nativeRequest = ((HttpServletRequest) request.getNativeRequest());
		Cookie sessionCookie = smU_Cookie.get(nativeRequest, type.getCookieName());
		smSessionCookieValue cookieValue = null;
		
		if( sessionCookie != null )
		{
			if( smU_Cookie.isDeleted(sessionCookie) )
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
				smA_JsonFactory jsonFactory = sm.jsonFactory;
				smI_JsonObject json = jsonFactory.createJsonObject(cookieValueJson);
				cookieValue = new smSessionCookieValue(json, type);
			}
			catch(Exception e)
			{
				s_logger.log(Level.SEVERE, "Could not parse cookie value's json ("+cookieValueJson+").", e);
			}
		}
		
		return cookieValue;
	}
	
	public void startSession(smUserSession userSession, Object nativeResponse, boolean rememberMe)
	{
		this.startSession(userSession, new smTransactionResponse(nativeResponse), rememberMe);
	}
	
	public void startSession(smUserSession userSession, smTransactionResponse response, boolean rememberMe)
	{
		smSessionCookieValue transCookieValue = createSessionCookieValue(response, userSession, smE_SessionType.TRANSIENT);
		smSessionCookieValue persCookieValue = rememberMe ? createSessionCookieValue(response, userSession, smE_SessionType.PERSISTENT) : null;
		
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
					HashMap<smI_BlobKey, smI_Blob> map = new HashMap<smI_BlobKey, smI_Blob>();
					map.put(transCookieValue, userSession);
					map.put(persCookieValue, userSession);
					
					m_blobManager.putBlobsAsync(map);
				}
			}
			catch(smBlobException e)
			{
				s_logger.log(Level.SEVERE, "Could not put session(s) into database for " + userSession.getAccountIdString());
			}
		}
	}
	
	private smUserSession getSessionByCookieValue(smSessionCookieValue cookieValue)
	{
		smUserSession userSession = null;
		
		try
		{
			userSession = m_blobManager.getBlob(cookieValue, smUserSession.class);
		}
		catch(smBlobException e)
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
				if( userSession.isExpired(smS_Session.SESSION_TIMEOUT) )
				{
					userSession = null;
					
					try
					{
						m_blobManager.deleteBlobAsync(cookieValue, smUserSession.class);
					}
					catch(smBlobException e)
					{
						s_logger.log(Level.SEVERE, "Could not delete expired session from db.", e);
					}
				}
			}
		}
		
		return userSession;
	}
	
	private smUserSession getSessionByType(smTransactionRequest request, smE_SessionType type)
	{
		smSessionCookieValue cookieValue = this.getSessionCookieValue(request, type);
		
		if( cookieValue == null )
		{
			return null;
		}
		
		return this.getSessionByCookieValue(cookieValue);
	}
	
	public smUserSession getSession(smTransactionRequest request, smTransactionResponse response)
	{
		//--- DRK > Early-out for when we have a session cached here.
		smUserSession userSession = m_sessionCache.get();
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
		
		userSession = this.getSessionByType(request, smE_SessionType.TRANSIENT);
		
		if( userSession == null)
		{
			smSessionCookieValue cookieValue = this.getSessionCookieValue(request, smE_SessionType.PERSISTENT);
			
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
						m_blobManager.deleteBlobAsync(cookieValue, smUserSession.class);
					}
					catch(smBlobException e)
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
		this.endSession(new smTransactionRequest(nativeRequest), new smTransactionResponse(nativeResponse));
	}
	
	public void endSession(smTransactionRequest request, smTransactionResponse response)
	{
		smSessionCookieValue transCookieValue = getSessionCookieValue(request, smE_SessionType.TRANSIENT);
		smSessionCookieValue persCookieValue = getSessionCookieValue(request, smE_SessionType.PERSISTENT);

		setCachedSession(m_nullSession);
		
		try
		{
			if( transCookieValue != null )
			{
				smU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), smE_SessionType.TRANSIENT.getCookieName());
				
				if( persCookieValue == null )
				{
					m_blobManager.deleteBlobAsync(transCookieValue, smUserSession.class);
				}
				else
				{
					smU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), smE_SessionType.PERSISTENT.getCookieName());
					
					HashMap<smI_BlobKey, Class<? extends smI_Blob>> map = new HashMap<smI_BlobKey, Class<? extends smI_Blob>>();
					map.put(transCookieValue, smUserSession.class);
					map.put(persCookieValue, smUserSession.class);
					m_blobManager.deleteBlobsAsync(map);
				}
			}
			else
			{
				s_logger.warning("Transient cookie value null when attempting to end session.");
				
				if( persCookieValue != null )
				{
					smU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), smE_SessionType.PERSISTENT.getCookieName());
					
					m_blobManager.deleteBlobAsync(persCookieValue, smUserSession.class);
				}
			}
		}
		catch(smBlobException e)
		{
			s_logger.log(Level.SEVERE, "Exception occured while trying to delete session(s) from database.");
		}
	}
	
	public boolean isSessionActive(smTransactionRequest request, smTransactionResponse response)
	{
		smUserSession session = this.getSession(request, response);
		
		return session != null;
	}
	
	public boolean isAuthorized(Object nativeRequest, Object nativeResponse, smE_Role requiredRole)
	{
		//--- DRK > Just creating dummy request/response wrappers here because caller obviously doesn't care about response.
		return isAuthorized(new smTransactionRequest(nativeRequest), new smTransactionResponse(nativeResponse), requiredRole);
	}
	
	public boolean isAuthenticated(Object nativeRequest, Object nativeResponse)
	{
		return isAuthorized(nativeRequest, nativeResponse, smE_Role.USER);
	}
	
	public boolean isAuthorized(smTransactionRequest request, smTransactionResponse response, smE_Role requiredRole)
	{
		boolean authorized = false;
		boolean authenticated = false;
		
		smUserSession userSession = getSession(request, response);
		
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
			response.setError(smE_ResponseError.NOT_AUTHENTICATED);
		}
		else if( !authorized )
		{
			response.setError(smE_ResponseError.NOT_AUTHORIZED);
		}
		
		return authorized;
	}
	
	private void setCachedSession(smUserSession session)
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
