package swarm.server.thirdparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import swarm.server.account.E_Role;
import swarm.server.account.ServerAccountManager;
import swarm.server.account.UserSession;
import swarm.server.app.A_ServerApp;
import swarm.server.app.A_ServerJsonFactory;
import swarm.server.app.ServerContext;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.ServerCell;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerCode;
import swarm.server.transaction.ServerTransactionManager;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignInValidator;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.thirdparty.S_Caja;
import swarm.shared.transaction.S_Transaction;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class CellPreviewServlet extends A_BaseServlet
{
	private static String s_previewHtml = null;
	
	private static final Logger s_logger = Logger.getLogger(CellPreviewServlet.class.getName());
	
	private static synchronized void initPreviewHtml(ServletContext context)
	{
		if( s_previewHtml != null )  return;
		
		s_previewHtml = U_Servlet.getResource(context, "/WEB-INF/home_cells/preview.html");
	}
	
	private static String getPreviewHtml(ServletContext context)
	{
		if( s_previewHtml == null )
		{
			initPreviewHtml(context);
		}
		
		return s_previewHtml;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGetOrPost(req, resp, true);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGetOrPost(req, resp, false);
	}
	
	private void doGetOrPost(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, boolean isGet) throws ServletException, IOException
	{
		//TODO: Get these from somewhere else.
		int defaultCellWidth = 512;
		int defaultCellHeight = 512;
		
		ServerContext context = A_ServerApp.getInstance().getContext();
		PrintWriter writer = nativeResponse.getWriter();
		String uri = nativeRequest.getRequestURI();
		E_CodeType codeType = E_CodeType.getCodeTypeFromURI(uri);
		boolean raw = uri.contains("raw");
		
		nativeResponse.setContentType("text/plain");
		nativeResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		
		if( codeType == null )
		{
			writer.write("Unrecognized code type!");
			
			return;
		}
		
		//TODO: Also allow cell address uri.
		String[] pathComponents = uri.split("/");
		String mappingString = pathComponents[pathComponents.length-1];
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(mappingString);
		
		
		I_BlobManager blobMngr = context.blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		try
		{
			ServerCell cell = blobMngr.getBlob(mapping, ServerCell.class);
			
			if( cell == null )
			{
				writer.write("Cell came up null!");
				
				return;
			}
			
			ServerCode code = (ServerCode) cell.getStandInCode(codeType);
			String rawCode = code.getRawCode();
			rawCode = rawCode != null ? rawCode : "";
			E_CodeSafetyLevel safetyLevel = code.getSafetyLevel();
			
			if( code != null )
			{
				if( codeType == E_CodeType.SOURCE )
				{
					writer.write(rawCode);
				}
				else
				{
					String previewHtml = getPreviewHtml(this.getServletContext());
					
					if( previewHtml != null )
					{
						if( codeType == E_CodeType.SPLASH || codeType == E_CodeType.COMPILED )
						{
							String html = "";
							
							if( raw )
							{
								html = rawCode;
								
								if( safetyLevel == E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX )
								{
									String namespace = mappingString+S_Caja.CAJA_NAMESPACE_SUFFIX;
									String cajaDivs = S_Caja.CAJA_DIVS_START.replaceAll("\\{\\{namespace\\}\\}", namespace);
									html = cajaDivs + html + S_Caja.CAJA_DIVS_END;
								}
							}
							else
							{
								CellSize cellSize = codeType == E_CodeType.COMPILED ? cell.getFocusedCellSize() : new CellSize();
								cellSize.setIfDefault(defaultCellWidth, defaultCellHeight);
								String cellSizeString = "width:"+cellSize.getWidth()+"px; height:"+cellSize.getHeight()+"px;";
								
								String frameSrc = "/r.preview/"+codeType.toString().toLowerCase()+"/raw/"+mappingString;
								previewHtml = previewHtml.replaceAll("\\{\\{cellSize\\}\\}", cellSizeString);
								previewHtml = previewHtml.replaceAll("\\{\\{frameSrc\\}\\}", frameSrc);
								
								html = previewHtml;
							}
							
							nativeResponse.setStatus(HttpServletResponse.SC_OK);
							nativeResponse.setContentType("text/html");
							writer.write(html);
						}
						else
						{
							writer.write("Code came up null!");
						}
					}
					else
					{
						writer.write("Preview html came up null!");
					}
				}
			}
			else
			{
				writer.write("Code came up null!");
			}
						
		}
		catch (BlobException e)
		{
			writer.write("Database Exception!");
		}
	}
}
