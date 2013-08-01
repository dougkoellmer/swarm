package com.b33hive.server.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.b33hive.server.json.bhServerJsonObject;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.transaction.bhS_Transaction;

public class bhU_Servlet
{
	private static final Logger s_logger = Logger.getLogger(bhU_Servlet.class.getName());
	
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
	
	public static void redirectToMainPage(HttpServletResponse response) throws IOException
	{
		// TODO: Should be app/host specific.
		response.sendRedirect("http://b33hive.net");
	}
	
	public static void simulateException(boolean simulate) throws Error
	{
		if( simulate )
		{
			throw new Error();
		}
	}
	
	static bhI_JsonObject getRequestJson(HttpServletRequest nativeRequest, boolean isGet)
	{
		String jsonString = null;
		if( isGet )
		{
			String jsonEncoded = nativeRequest.getParameter(bhS_Transaction.JSON_URL_PARAM);
			
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
		
		bhI_JsonObject requestJson = jsonString == null ? null : bhA_JsonFactory.getInstance().createJsonObject(jsonString);
		
		return requestJson;
	}
	
	static void writeJsonResponse(bhI_JsonObject responseJson, Writer writer) throws IOException
	{
		try
		{
			((bhServerJsonObject)responseJson).getNative().write(writer);
		}
		catch (JSONException e)
		{
			s_logger.log(Level.SEVERE, "Could not write json to response stream.", e);
			
			throw new IOException(e);
		}
	}
}
