package swarm.server.session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class U_Cookie
{
	private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
	private static final String DELETION_EXPIRATION = "Tue, 15-Jan-1970 21:47:38 GMT";
	private static final String COOKIE_HEADER = "Set-Cookie";
	private static final String DELETED_VALUE = "deleted";
	
	public static Cookie get(HttpServletRequest request, String name)
	{
		Cookie[] cookies = request.getCookies();
		
		if( cookies != null )
		{
			for( int i = cookies.length-1; i >= 0; i-- )
			{
				Cookie ithCookie = cookies[i];
				
				if( ithCookie.getName().equals(name) )
				{
					return ithCookie;
				}
			}
		}
		
		return null;
	}
	
	public static boolean isDeleted(Cookie cookie)
	{
		return cookie.getValue().equals(DELETED_VALUE);
	}
	
	public static void delete(HttpServletResponse response, String name)
	{
		String headerValue = name+"="+DELETED_VALUE+"; Path=/; Expires="+DELETION_EXPIRATION+"; HttpOnly";
		
		response.addHeader(COOKIE_HEADER, headerValue);
	}
	
	public static void add(HttpServletResponse response, String name, String value, Long maxAge, boolean httpOnly)
	{
		String expires = null;
		
		if( maxAge != null )
		{
			Date expdate = new Date();
			expdate.setTime (expdate.getTime() + (maxAge * 1000));
			DateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.US);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			expires = df.format(expdate);
		}
		
		String headerValue = name+"="+value+"; Path=/";
		
		if( expires != null )
		{
			headerValue += "; Expires="+expires;
		}
		
		if( httpOnly )
		{
			headerValue += "; HttpOnly";
		}
		
		response.addHeader(COOKIE_HEADER, headerValue);
	}
	
	public static void add(HttpServletResponse response, String name, String value, boolean httpOnly)
	{
		add(response, name, value, null, httpOnly);
	}
}
