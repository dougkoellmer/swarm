package swarm.server.thirdparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import swarm.server.app.A_ServerApp;
import swarm.server.app.ServerContext;
import swarm.server.thirdparty.json.ServerJsonObject;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.transaction.S_Transaction;


public class U_Servlet
{
	private static final Logger s_logger = Logger.getLogger(U_Servlet.class.getName());
	
	static void simulateLag(long milliseconds)
	{
		if( milliseconds == 0 )  return;
		
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e1)
		{
		}
	}
	
	public static boolean fileExists(ServletContext context, String file)
	{
		InputStream stream = context.getResourceAsStream(file);
		return stream != null;
	}
	
	public static String getResource(ServletContext context, String file)
	{
		InputStream stream = context.getResourceAsStream(file);
		String resource = "";
		
		if( stream != null )
		{
			try
			{
				resource = IOUtils.toString(stream, "UTF-8");
			}
			catch (IOException e)
			{
				s_logger.severe("Couldn't load resource: " + file);
			}
		}
		
		return resource;
	}
	
	public static void simulateException(boolean simulate) throws Error
	{
		if( simulate )
		{
			throw new Error();
		}
	}
	
	static I_JsonObject getRequestJson(HttpServletRequest nativeRequest, boolean isGet)
	{
		String jsonString = null;
		if( isGet )
		{
			String jsonEncoded = nativeRequest.getParameter(S_Transaction.JSON_URL_PARAM);
			
			//TODO: Fix deprecation error
			jsonString = URLDecoder.decode(jsonEncoded);
		}
		else
		{
			StringBuffer jb = new StringBuffer();
			String line = null;
			
			try
			{
				BufferedReader reader = nativeRequest.getReader();
				while ((line = reader.readLine()) != null)
				{
					jb.append(line);
				}
				
				jsonString = jb.toString();
			}
			catch (Exception e)
			{
				s_logger.log(Level.SEVERE, "Could not read json from request stream.", e);
				
				jsonString = null;
			}
		}
		
		ServerContext context = A_ServerApp.getInstance().getContext();
		I_JsonObject requestJson = jsonString == null ? null : context.jsonFactory.createJsonObject(jsonString);
		
		return requestJson;
	}
	
	static void writeJsonResponse(I_JsonObject responseJson, Writer writer) throws IOException
	{
		try
		{
			((ServerJsonObject)responseJson).getNative().write(writer);
		}
		catch (JSONException e)
		{
			s_logger.log(Level.SEVERE, "Could not write json to response stream.", e);
			
			throw new IOException(e);
		}
	}
}
