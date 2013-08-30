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

import swarm.server.thirdparty.json.smServerJsonObject;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smS_Transaction;


public class smU_Servlet
{
	private static final Logger s_logger = Logger.getLogger(smU_Servlet.class.getName());
	
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
	
	static smI_JsonObject getRequestJson(HttpServletRequest nativeRequest, boolean isGet)
	{
		String jsonString = null;
		if( isGet )
		{
			String jsonEncoded = nativeRequest.getParameter(smS_Transaction.JSON_URL_PARAM);
			
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
		
		smI_JsonObject requestJson = jsonString == null ? null : smSharedAppContext.jsonFactory.createJsonObject(jsonString);
		
		return requestJson;
	}
	
	static void writeJsonResponse(smI_JsonObject responseJson, Writer writer) throws IOException
	{
		try
		{
			((smServerJsonObject)responseJson).getNative().write(writer);
		}
		catch (JSONException e)
		{
			s_logger.log(Level.SEVERE, "Could not write json to response stream.", e);
			
			throw new IOException(e);
		}
	}
}
