package com.b33hive.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.b33hive.client.app.bhPlatformInfo;
import com.b33hive.client.transaction.bhClientTransactionManager;
import com.b33hive.server.account.bhAccountDatabase;
import com.b33hive.server.account.bhServerAccountManager;
import com.b33hive.server.code.bhServerCodeCompiler;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.handlers.*;
import com.b33hive.server.handlers.admin.clearCell;
import com.b33hive.server.handlers.admin.createGrid;
import com.b33hive.server.handlers.admin.deactivateUserCells;
import com.b33hive.server.handlers.admin.recompileCells;
import com.b33hive.server.handlers.admin.refreshHomeCells;
import com.b33hive.server.json.bhServerJsonFactory;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.telemetry.bhTelemetryDatabase;
import com.b33hive.server.transaction.bhE_AdminRequestPath;
import com.b33hive.server.transaction.bhE_DebugRequestPath;
import com.b33hive.server.transaction.bhInlineTransactionManager;
import com.b33hive.server.transaction.bhServerTransactionManager;
import com.b33hive.shared.bhE_AppEnvironment;
import com.b33hive.shared.app.bhA_App;
import com.b33hive.shared.debugging.bhI_AssertionDelegate;
import com.b33hive.shared.debugging.bhTelemetryAssert;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_TelemetryRequestPath;
import com.b33hive.shared.transaction.bhRequestPathManager;
import com.google.appengine.api.rdbms.AppEngineDriver;
import com.google.gwt.user.client.Window;

public final class bhServerApp extends bhA_App
{
	private static final Logger s_logger = Logger.getLogger(bhServerApp.class.getName());

	private bhServerApp()
	{
		super(bhE_AppEnvironment.SERVER);
	}
	
	public static bhServerApp getInstance()
	{
		return (bhServerApp) s_instance;
	}
	
	//TODO: Should probably throw an exception type, perhaps custom.
	public static synchronized void entryPoint()
	{
		if( s_instance != null )
		{
			s_logger.warning("Attempted to start application twice.");
			
			return;
		}
		
		s_instance = new bhServerApp();
		
		bhU_Debug.setDelegate(new bhI_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				assert(false);
			}
		});
		
		new bhServerJsonFactory(); // DRK > initializes bhA_JsonFactory singleton.
		
		new bhServerCodeCompiler(); // initializes compiler singleton.
		
		try
		{
			DriverManager.registerDriver(new AppEngineDriver());
		}
		catch (SQLException e1)
		{
			s_logger.log(Level.SEVERE, "Could not start up sql databases.", e1);
		}

		bhBlobManagerFactory.startUp();
		bhSessionManager.startUp();
		bhServerAccountManager.startUp();
		bhServerTransactionManager.startUp();
		bhTelemetryDatabase.startUp();
		bhAccountDatabase.startUp();
		
		addClientHandlers();
		addAdminHandlers();
		addTelemetryHandlers();
		//addDebugHandlers();
		//addDebugPathResponseErrors();
		
		bhServerTransactionManager.getInstance().addScopeListener(bhBlobManagerFactory.getInstance());
		bhServerTransactionManager.getInstance().addScopeListener(bhSessionManager.getInstance());
		bhServerTransactionManager.getInstance().addScopeListener(bhAccountDatabase.getInstance());
		bhServerTransactionManager.getInstance().addScopeListener(bhTelemetryDatabase.getInstance());
	}
	
	private static void addDebugPathResponseErrors()
	{
		bhServerTransactionManager manager = bhServerTransactionManager.getInstance();
		manager.setDebugResponseError(bhE_RequestPath.syncCode);
	}
	
	private static void addClientHandlers()
	{
		bhRequestPathManager.getInstance().register(bhE_RequestPath.values());
		
		bhServerTransactionManager manager = bhServerTransactionManager.getInstance();
		
		getCode getCodeHandler = new getCode();
		
		manager.addRequestHandler(getCodeHandler,				bhE_RequestPath.getCode);
		manager.addRequestHandler(new syncCode(),				bhE_RequestPath.syncCode);
		manager.addRequestHandler(new getCellAddress(),			bhE_RequestPath.getCellAddress);
		manager.addRequestHandler(new getCellAddressMapping(),	bhE_RequestPath.getCellAddressMapping);
		manager.addRequestHandler(new getUserData(),			bhE_RequestPath.getUserData);
		manager.addRequestHandler(new getGridData(),			bhE_RequestPath.getGridData);
		manager.addRequestHandler(new signIn(),					bhE_RequestPath.signIn);
		manager.addRequestHandler(new signUp(),					bhE_RequestPath.signUp);
		manager.addRequestHandler(new signOut(),				bhE_RequestPath.signOut);
		manager.addRequestHandler(new getAccountInfo(),			bhE_RequestPath.getAccountInfo);
		manager.addRequestHandler(new setNewDesiredPassword(),	bhE_RequestPath.setNewDesiredPassword);
		manager.addRequestHandler(new getPasswordChangeToken(),	bhE_RequestPath.getPasswordChangeToken);
		manager.addRequestHandler(new getServerVersion(),		bhE_RequestPath.getServerVersion);
		
		manager.addDeferredHandler(getCodeHandler);
	}
	
	private static void addAdminHandlers()
	{
		bhRequestPathManager.getInstance().register(bhE_AdminRequestPath.values());
		
		bhServerTransactionManager manager = bhServerTransactionManager.getInstance();
		
		manager.addRequestHandler(new createGrid(),					bhE_AdminRequestPath.createGrid);
		manager.addRequestHandler(new deactivateUserCells(),		bhE_AdminRequestPath.deactivateUserCells);
		manager.addRequestHandler(new refreshHomeCells(),			bhE_AdminRequestPath.refreshHomeCells);
		manager.addRequestHandler(new clearCell(),					bhE_AdminRequestPath.clearCell);
		manager.addRequestHandler(new recompileCells(),				bhE_AdminRequestPath.recompileCells);
	}
	
	private static void addTelemetryHandlers()
	{
		bhRequestPathManager.getInstance().register(bhE_TelemetryRequestPath.values());
		
		bhServerTransactionManager manager = bhServerTransactionManager.getInstance();
		
		manager.addRequestHandler(new logAssert(),		bhE_TelemetryRequestPath.logAssert);
	}
	
	private static void addDebugHandlers()
	{
		bhRequestPathManager.getInstance().register(bhE_DebugRequestPath.values());
		
		bhServerTransactionManager manager = bhServerTransactionManager.getInstance();
		
		manager.addRequestHandler(new sessionQueryTest(),		bhE_DebugRequestPath.sessionQueryTest);
	}
}
