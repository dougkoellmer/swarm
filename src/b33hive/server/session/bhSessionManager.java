package b33hive.server.session;

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

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhS_ServerAccount;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.data.blob.bhU_Serialization;
import b33hive.server.transaction.bhI_TransactionScopeListener;
import b33hive.shared.app.bh;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;


public class bhSessionManager implements bhI_TransactionScopeListener
{
	private static final Logger s_logger = Logger.getLogger(bhSessionManager.class.getName());
		
	private final URLCodec m_urlCodec = new URLCodec(); // thread safe
	
	private bhI_BlobManager m_blobManager;
	
	private final ThreadLocal<bhUserSession> m_sessionCache = new ThreadLocal<bhUserSession>();
	
	private final bhUserSession m_nullSession = new bhUserSession();
	
	public bhSessionManager()
	{
		m_blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
	}
	
	private bhSessionCookieValue createSessionCookieValue(bhTransactionResponse response, bhUserSession userSession, bhE_SessionType type)
	{
		HttpServletResponse nativeResponse = ((HttpServletResponse) response.getNativeResponse());
		bhSessionCookieValue cookieValue = new bhSessionCookieValue(userSession.getAccountId(), type);
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
		
		if( type == bhE_SessionType.PERSISTENT )
		{
			bhU_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, bhS_Session.SESSION_TIMEOUT, true);
		}
		else
		{
			bhU_Cookie.add(nativeResponse, type.getCookieName(), cookieValueJson, true);
		}
		
		return cookieValue;
	}
	
	private bhSessionCookieValue getSessionCookieValue(bhTransactionRequest request, bhE_SessionType type)
	{
		HttpServletRequest nativeRequest = ((HttpServletRequest) request.getNativeRequest());
		Cookie sessionCookie = bhU_Cookie.get(nativeRequest, type.getCookieName());
		bhSessionCookieValue cookieValue = null;
		
		if( sessionCookie != null )
		{
			if( bhU_Cookie.isDeleted(sessionCookie) )
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
				bhA_JsonFactory jsonFactory = bh.jsonFactory;
				bhI_JsonObject json = jsonFactory.createJsonObject(cookieValueJson);
				cookieValue = new bhSessionCookieValue(json, type);
			}
			catch(Exception e)
			{
				s_logger.log(Level.SEVERE, "Could not parse cookie value's json ("+cookieValueJson+").", e);
			}
		}
		
		return cookieValue;
	}
	
	public void startSession(bhUserSession userSession, bhTransactionResponse response, boolean rememberMe)
	{
		bhSessionCookieValue transCookieValue = createSessionCookieValue(response, userSession, bhE_SessionType.TRANSIENT);
		bhSessionCookieValue persCookieValue = rememberMe ? createSessionCookieValue(response, userSession, bhE_SessionType.PERSISTENT) : null;
		
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
					HashMap<bhI_BlobKeySource, bhI_Blob> map = new HashMap<bhI_BlobKeySource, bhI_Blob>();
					map.put(transCookieValue, userSession);
					map.put(persCookieValue, userSession);
					
					m_blobManager.putBlobsAsync(map);
				}
			}
			catch(bhBlobException e)
			{
				s_logger.log(Level.SEVERE, "Could not put session(s) into database for " + userSession.getAccountIdString());
			}
		}
	}
	
	private bhUserSession getSessionByCookieValue(bhSessionCookieValue cookieValue)
	{
		bhUserSession userSession = null;
		
		try
		{
			userSession = m_blobManager.getBlob(cookieValue, bhUserSession.class);
		}
		catch(bhBlobException e)
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
				if( userSession.isExpired(bhS_Session.SESSION_TIMEOUT) )
				{
					userSession = null;
					
					try
					{
						m_blobManager.deleteBlobAsync(cookieValue, bhUserSession.class);
					}
					catch(bhBlobException e)
					{
						s_logger.log(Level.SEVERE, "Could not delete expired session from db.", e);
					}
				}
			}
		}
		
		return userSession;
	}
	
	private bhUserSession getSessionByType(bhTransactionRequest request, bhE_SessionType type)
	{
		bhSessionCookieValue cookieValue = this.getSessionCookieValue(request, type);
		
		if( cookieValue == null )
		{
			return null;
		}
		
		return this.getSessionByCookieValue(cookieValue);
	}
	
	public bhUserSession getSession(bhTransactionRequest request, bhTransactionResponse response)
	{
		//--- DRK > Early-out for when we have a session cached here.
		bhUserSession userSession = m_sessionCache.get();
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
		
		userSession = this.getSessionByType(request, bhE_SessionType.TRANSIENT);
		
		if( userSession == null)
		{
			bhSessionCookieValue cookieValue = this.getSessionCookieValue(request, bhE_SessionType.PERSISTENT);
			
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
						m_blobManager.deleteBlobAsync(cookieValue, bhUserSession.class);
					}
					catch(bhBlobException e)
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
	
	public void endSession(bhTransactionRequest request, bhTransactionResponse response)
	{
		bhSessionCookieValue transCookieValue = getSessionCookieValue(request, bhE_SessionType.TRANSIENT);
		bhSessionCookieValue persCookieValue = getSessionCookieValue(request, bhE_SessionType.PERSISTENT);

		setCachedSession(m_nullSession);
		
		try
		{
			if( transCookieValue != null )
			{
				bhU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), bhE_SessionType.TRANSIENT.getCookieName());
				
				if( persCookieValue == null )
				{
					m_blobManager.deleteBlobAsync(transCookieValue, bhUserSession.class);
				}
				else
				{
					bhU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), bhE_SessionType.PERSISTENT.getCookieName());
					
					HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> map = new HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>>();
					map.put(transCookieValue, bhUserSession.class);
					map.put(persCookieValue, bhUserSession.class);
					m_blobManager.deleteBlobsAsync(map);
				}
			}
			else
			{
				s_logger.warning("Transient cookie value null when attempting to end session.");
				
				if( persCookieValue != null )
				{
					bhU_Cookie.delete((HttpServletResponse) response.getNativeResponse(), bhE_SessionType.PERSISTENT.getCookieName());
					
					m_blobManager.deleteBlobAsync(persCookieValue, bhUserSession.class);
				}
			}
		}
		catch(bhBlobException e)
		{
			s_logger.log(Level.SEVERE, "Exception occured while trying to delete session(s) from database.");
		}
	}
	
	public boolean isSessionActive(bhTransactionRequest request, bhTransactionResponse response)
	{
		bhUserSession session = this.getSession(request, response);
		
		return session != null;
	}
	
	public boolean isAuthorized(Object nativeRequest, Object nativeResponse, bhE_Role requiredRole)
	{
		//--- DRK > Just creating dummy request/response wrappers here because caller obviously doesn't care about response.
		return isAuthorized(new bhTransactionRequest(nativeRequest), new bhTransactionResponse(nativeResponse), requiredRole);
	}
	
	public boolean isAuthorized(bhTransactionRequest request, bhTransactionResponse response, bhE_Role requiredRole)
	{
		boolean authorized = false;
		boolean authenticated = false;
		
		bhUserSession userSession = getSession(request, response);
		
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
			response.setError(bhE_ResponseError.NOT_AUTHENTICATED);
		}
		else if( !authorized )
		{
			response.setError(bhE_ResponseError.NOT_AUTHORIZED);
		}
		
		return authorized;
	}
	
	private void setCachedSession(bhUserSession session)
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
